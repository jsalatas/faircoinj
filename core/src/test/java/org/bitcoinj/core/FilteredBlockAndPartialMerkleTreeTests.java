/*
 * Copyright 2012 Matt Corallo
 * Copyright 2014 Andreas Schildbach
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bitcoinj.core;

import com.google.common.collect.*;
import org.bitcoinj.core.TransactionConfidence.*;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.store.*;
import org.bitcoinj.testing.*;
import org.bitcoinj.wallet.*;
import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;

import java.io.IOException;
import java.io.OutputStream;
import java.math.*;
import java.util.*;

import static org.bitcoinj.core.Utils.*;
import static org.junit.Assert.*;

@RunWith(value = Parameterized.class)
public class FilteredBlockAndPartialMerkleTreeTests extends TestWithPeerGroup {
    @Parameterized.Parameters
    public static Collection<ClientType[]> parameters() {
        return Arrays.asList(new ClientType[] {ClientType.NIO_CLIENT_MANAGER},
                             new ClientType[] {ClientType.BLOCKING_CLIENT_MANAGER});
    }

    public FilteredBlockAndPartialMerkleTreeTests(ClientType clientType) {
        super(clientType);
    }

    @Before
    public void setUp() throws Exception {
        context = new Context(UNITTEST);
        MemoryBlockStore store = new MemoryBlockStore(UNITTEST);

        // Cheat and place the previous block (block 81890) at the head of the block store without supporting blocks
        Block b = new Block(UNITTEST, HEX.decode("01010000fa3b0c1a50a6f44d284caa7727b4af964b5ed86b5f465867bb6595f1de3e38c4201af7096947c1e4e95cc9a33f0c41e9ca1e45ffa4199927bd9efe4e204fc97b8e511426695dccd99fab287da4eef141fb330ebbab8cb98d45f9d233c9b65fbb4e7c4f5a5cae5fae0201000000010000000000000000000000000000000000000000000000000000000000000000ffffffff0803e23f0103558c07ffffffff01e86e0300000000001976a91465a17f523bc616317542e58c28533682efe5fdd788ac00000000010000000100b76d198e80e659ce25db9d6f1537424e00f5af65fc982132c948690532aae0000000006a47304402204d51545b3c4eee4693b0693c328bfc477497079b4e50fe8fa2bd2251012604730220071c9f667720297620d839d41943aa59992e299dfef9e955fbf68f377551a2ad0121027f1ba11b094ab8c962cf1efada3afb539b59b4badbf57cf042f229c848d2e5c4feffffff0200aea68f020000001976a914cdf3a40415ef57febbf944b939377297b10452a888aca89ed560050000001976a91480f5950267d5f179a15ef956061a0da75b1e2c6988ac8f3f0100ad6ff226ebc3994080b8adb92ddc196a47364598935fe7f2d06ccf7f41b5d4fe39cce4adf68f51001ec7ead8109ddb60871bc03304b8c06960148b3e931afc64002de33721290d2f9949b270fa6048b1429568062df0663b7a8d5227e1e71851f8c8d40019a1795b0746bc373c798f7785a437059a19e977bcd2d506d492a933aa"));
        store.put(new StoredBlock(b, BigInteger.valueOf(1), 81890));
        store.setChainHead(store.get(Sha256Hash.wrap("e41da595e9a95f3d50abc7c7609eccfae9938b79816e2f0b57d369c785798e6e")));

        KeyChainGroup group = KeyChainGroup.builder(UNITTEST).build();

        wallet = new Wallet(UNITTEST, group);

        wallet.addWatchedAddress(LegacyAddress.fromPubKeyHash(MainNetParams.get(), HEX.decode("0fb3107609cb2e68fefc4236817b5db703ad9a9a")));
        wallet.addWatchedAddress(LegacyAddress.fromPubKeyHash(MainNetParams.get(), HEX.decode("1aec0f7b7bbd85dd8c9d13594c3e9346521ef493")));
        wallet.addWatchedAddress(LegacyAddress.fromPubKeyHash(MainNetParams.get(), HEX.decode("827faf94ba53d5c38c200fe274873644f3ce4bb6")));
        wallet.addWatchedAddress(LegacyAddress.fromPubKeyHash(MainNetParams.get(), HEX.decode("750ba28e54682792ca4c5770fc29beda9baff0bf")));
        super.setUp(store);
    }

    @Override
    @After
    public void tearDown() {
        super.tearDown();
    }

    @Test
    public void deserializeFilteredBlock() throws Exception {
        // Random real block (133a0718b22923ab5f9404be4ee61c948b6e3748c5aaae4d67751bcbd8897eac)
        // With two tx
        FilteredBlock block = new FilteredBlock(UNITTEST, HEX.decode("01010000983adf51be400a5f560b14357ea000bf0235e7e02153b80b3c01fbd32e93100d5e9a4f00ab4ddcf2422251a658fae0eb3141e6aa9744369e7e4c6e6137d82c6c409beb25d9313c4b15165b2dc8cab33dc40ae601ba690696a3248df2fcaffda8e362815afecafeca0200000002c24c6d3857c10fb9b7fc64edb16fccdb90a095ed57483bb15a3a9d01044f56da43c21c2ad74a34e8b384c3e390bb06668c2fe9f60d4eb9e9dc29987aebb23a930107"));
        
        // Check that the header was properly deserialized
        assertTrue(block.getBlockHeader().getHash().equals(Sha256Hash.wrap("133a0718b22923ab5f9404be4ee61c948b6e3748c5aaae4d67751bcbd8897eac")));
        
        // Check that the partial merkle tree is correct
        List<Sha256Hash> txesMatched = block.getTransactionHashes();
        assertTrue(txesMatched.size() == 2);
        assertTrue(txesMatched.contains(Sha256Hash.wrap("da564f04019d3a5ab13b4857ed95a090dbcc6fb1ed64fcb7b90fc157386d4cc2")));
        assertTrue(txesMatched.contains(Sha256Hash.wrap("933ab2eb7a9829dce9b94e0df6e92f8c6606bb90e3c384b3e8344ad72a1cc243")));

        // Check round tripping.
        assertEquals(block, new FilteredBlock(UNITTEST, block.bitcoinSerialize()));
    }

    @Test
    public void createFilteredBlock() throws Exception {
        ECKey key1 = new ECKey();
        ECKey key2 = new ECKey();
        Transaction tx1 = FakeTxBuilder.createFakeTx(UNITTEST, Coin.COIN,  key1);
        Transaction tx2 = FakeTxBuilder.createFakeTx(UNITTEST, Coin.FIFTY_COINS, LegacyAddress.fromKey(UNITTEST, key2));
        Block block = FakeTxBuilder.makeSolvedTestBlock(UNITTEST.getGenesisBlock(), LegacyAddress.fromBase58(UNITTEST, "msg2t2V2sWNd85LccoddtWysBTR8oPnkzW"), tx1, tx2);
        BloomFilter filter = new BloomFilter(4, 0.1, 1);
        filter.insert(key1);
        filter.insert(key2);
        FilteredBlock filteredBlock = filter.applyAndUpdate(block);
        assertEquals(4, filteredBlock.getTransactionCount());
        // This call triggers verification of the just created data.
        List<Sha256Hash> txns = filteredBlock.getTransactionHashes();
        assertTrue(txns.contains(tx1.getTxId()));
        assertTrue(txns.contains(tx2.getTxId()));
    }

    private Sha256Hash numAsHash(int num) {
        byte[] bits = new byte[32];
        bits[0] = (byte) num;
        return Sha256Hash.wrap(bits);
    }

    @Test(expected = VerificationException.class)
    public void merkleTreeMalleability() throws Exception {
        List<Sha256Hash> hashes = Lists.newArrayList();
        for (byte i = 1; i <= 10; i++) hashes.add(numAsHash(i));
        hashes.add(numAsHash(9));
        hashes.add(numAsHash(10));
        byte[] includeBits = new byte[2];
        Utils.setBitLE(includeBits, 9);
        Utils.setBitLE(includeBits, 10);
        PartialMerkleTree pmt = PartialMerkleTree.buildFromLeaves(UNITTEST, includeBits, hashes);
        List<Sha256Hash> matchedHashes = Lists.newArrayList();
        pmt.getTxnHashAndMerkleRoot(matchedHashes);
    }

    @Test
    public void serializeDownloadBlockWithWallet() throws Exception {
        // First we create all the necessary objects, including lots of serialization and double-checks
        // Note that all serialized forms here are generated by Bitcoin Core/pulled from block explorer
        Block block = new Block(UNITTEST, HEX.decode("010100006e8e7985c769d3570b2f6e81798b93e9facc9e60c7c7ab503d5fa9e995a51de4017abb56aa9bf8bc411570c73b4b90e1c602d172291e3f08f2a9ce9dd6ce1e24d81f9653d5daf180efff35c7e9c155c28b509158ea264e065e06c435148e31ee027d4f5a66519e840c01000000010000000000000000000000000000000000000000000000000000000000000000ffffffff0703e33f01025c14ffffffff01c0d8a700000000001976a9140fb3107609cb2e68fefc4236817b5db703ad9a9a88ac000000000100000001ecf65f112e2d28c2b47b25d52719c73be8bcc949241a166f1cd391c18019ada4010000006a47304402200de99e40748a93f506ed6780b714b0124df466bbe6bf8141819a856773dca9ff022053891a57565a8948d7047c231f92e3d69563e447ce67ab7103e3f59b6c52b3a40121028e624b9dbb164d7e930f45b4b064050890972b9118605325298deb53babf2baaffffffff01c0312ca40b0000001976a9141aec0f7b7bbd85dd8c9d13594c3e9346521ef49388ac000000000100000001796daca022e9d274f4064b0ecc533d69fe2bba86c118a82aa7263cdac0737d10010000006a47304402200144864809c861eb2af4ee256d89afe4ec0ebb77f14a1851dadda6bc6f649bc50220224e4074593dafc8fef6d30cca992713353b7d7d6259717ee6597907b6c46706012103c7c8b6629e3167c5497ec9120fcb450c294180ae98052c37150802989acbcd70ffffffff01c0512677000000001976a914827faf94ba53d5c38c200fe274873644f3ce4bb688ac00000000010000000106585f12c6c7ebe8d438fee6a6827430468619c789c9f47795abe475eee9083f000000006a47304402203ea0c7b00789d44e3c8c8ec0d064ec10ac227387da252f5f5c61ffa8e58eff87022029f85b5d41dcdd64a61bda73ceaf02a84d0bf37754164d327ff183b1fe8f1a2f0121038c5238ea62ff748a06e78c25d2bdb3489d842534b80c99f79b353684e4f4e697ffffffff02304fe80b000000001976a914f1a0d60e30028eb1940e888d32f94d3ebc10e75f88acc01bc1b2000000001976a914053bbf09b31ae26ecc53499d42f76a9eff69e8d688ac0000000001000000010149ce0d1f64351ecef80dcb88456be1eadbad86c06d7b1f5d6cf9e1e95a4647010000006a4730440220168ef4ae2af27fcd32091a8d9907349dd60c56387bb5ff43bd7b70f001df01b20220702f04a7f18486f3853cb76f009794456295e939b78b1dba5f2d3a3dad2872e2012103fe4e7285c636d2e7f844cc3e358c23d5c750420b8bf5821f4261df196753fda1ffffffff02c0a1fc53020000001976a9149fb61a5a063ee335d827c22523cf7beed39cfc9188ac605ca10e110000001976a914be0df4b047747cde3bb0ea012e45972d502fba2888ac00000000010000000104ad09e44c47db456a2896270cc0299234123d753ada9dc86cce6f6cd7da0ffa010000006a47304402206b44f7fdc9b4d4ef8d7f3bb06d4c36079e666090c9bc3fe8d99b6bd3283be3c5022038e8326e587599b6a26b0504da3f423de16b365465c23608e97b28a91ba04519012103286cac9ff005529b8a5180eca88d3679cae8fa673f84344f4de30c4b49a3554effffffff0200ca9a3b000000001976a914af55f07da3748d19b1486f4fd7ac29e96ff8ed4988acc0a1fc53020000001976a91417969354637d6333e07c2ccc7fac1f15871d9e6d88ac0000000001000000012b800a005ee45a61da805c564735fa8f4b0ecbe9d57ca40910a8e8c08573e34a000000006a47304402207e338195ab25c9ce2ffdb5eaa8e3f5a2617e91b7c67454f326d75958ebb2f90c022076f05eb463a312a773eb2daa21f81107a8a31204b9caa4f85886c7bdf229bd2c01210369279daaa48172ed6443127003c3bf9522427efc083faac6cb427e7614ea32e6ffffffff02c09ee605000000001976a914750ba28e54682792ca4c5770fc29beda9baff0bf88acc0f66b12020000001976a914d0290f0c574b553f0937a4e1615be43ebdeea77e88ac000000000100000001cf6938085c99acda5792479229d32ea37b52d7b7472560aec66904f0b6e4a4c9000000006b483045022100a920e5d6d24e7398554aad3cb6c4bfd9a24c21541303957cf25047e9cabd243002202f51e0bb568084ec45c33a75d45fb043bf839862d7cce2350cba4c1a2cb445150121028e624b9dbb164d7e930f45b4b064050890972b9118605325298deb53babf2baaffffffff02c06b978f020000001976a91468ce6e8640a890ec5382f6c5c8bfa80f862ef55988acc07eec320b0000001976a914c6cdaf804bae4e8637e5b4be73f8943d9bb2fb3588ac00000000010000000266354e2a855d32f7ae57fd94e32936fbd575ae67200a2ca160ff412b947d8655010000006b483045022100d59e0412ee59d67e68706407b6bc22785e253f8d534f830d086869b06b93e1b4022036a57362e0c44a7b1a1b5bb75d3b7093e0276c677e6ae62123ab925acc44b32c012103327c7a4f6d82ba3b18fa6d84397432b2d236e603c4e6658dd0946274f33fc72bffffffff8de87a5dc9bb3c7d143ed00cfe5b8db890f4b5bcc5b8c9b29c1ac86d965986fc010000006b483045022100bada6d4724f4056e9ad0fae76ffd85636a6a0b68031ff1d154b8492c4172b85a022037baccfd4f0519d0b2cd983fe4e7112dea188a20659194534c7b85e725ab54e5012103327c7a4f6d82ba3b18fa6d84397432b2d236e603c4e6658dd0946274f33fc72bffffffff01c05dcc215d0000001976a9142915d9cbaf81b11a53da1bf0021c64253e03c38288ac000000000100000002fae0810390c37cc9ace8df9ac265152018bace5fee83d19d88078f97796866b6000000006b483045022100fe8fde3c88ae0480a9f0e311d33c636a2e8a73ec781f87e90e12497593108420022030705dfc47ecd3fe1cf381bc42308d99a65a7678d1d90116d19469a7e56a1ec201210314ce3ae3b3e462b0cdc76d26498b59a6f857afdc2870b04e75f05c5ef76087feffffffff2ddca9f07ced67fbd921bb5c111f38d5cd537778a972c8230d941fe400fde8c9010000006b483045022100e73f8fce69151ffd690526df865ae4b76b3bc887afe8a3930d7be50e1c7b8fb202202f45190dd99133f8274f7994955694f0eb012b180475b3024c0a480a37c43b2d01210314ce3ae3b3e462b0cdc76d26498b59a6f857afdc2870b04e75f05c5ef76087feffffffff02402e829b000000001976a914358fce266218f95ec78687599a18c0699285b48688acc0eb4976110000001976a914cdf3a40415ef57febbf944b939377297b10452a888ac000000000100000004539ec6d49631c2cf609c2cbd03ef56c331b9e901b6ae3b8c45bfc6225ca81c08040000006b483045022100a62c20e998a85000f7b10b83d0419614c61c0e6a85b5e6fbb5219d27f539ce7b02204e669e512fd252e1ccb04d31b257e17761050f5de2c7cf934db26ec03df1b074012103a88b639b752a7759774a717f83bebb90b0cbe587acd736c0bef0193e86e7dbb6ffffffff6ca22ca37315be784250e0bc1dcac7ef42eda744b10bb6d223ddf6bd38319c2c000000006b483045022100817ab56817d10d2f3902f15923f8ef3d0544669c2be5499a60bc9ac3f13cea1002200f77cc1448ffeee165620c510cee36dccbab530b6bab96fb53fe2e4c8cfac589012103a88b639b752a7759774a717f83bebb90b0cbe587acd736c0bef0193e86e7dbb6ffffffffd3171a55207f97eef87f16ab2dd59d15ab6360e050b16444bbd645b3d883e5f1020000006a47304402201a902941a4004581f7f573dcc76620212b5e5709836690fa2bea66b632d8bf52022057c551a8f60b8df30563af7e234885f076ede6f3b2d0f47110232738507f2e54012103a88b639b752a7759774a717f83bebb90b0cbe587acd736c0bef0193e86e7dbb6ffffffff8eefdd2cbc5b757ecb7db7c66ba9496ddab5292fb50118c33e7f5e756b4c19f8010000006a4730440220238323fd1101d89d7b7790a1d30454d7853ab5dc52d0667934de33dfcbcf1c6a02203b56c3e8774408850a8b745eefb097b0754f7cc49f38e7a8efb0d0339e82f3f5012103a88b639b752a7759774a717f83bebb90b0cbe587acd736c0bef0193e86e7dbb6ffffffff02e0259906000000001976a914bbdae2e4162bc1c9b3951f775a452af2ecbc850a88acc0c613e00c0000001976a914a094555c7aff0a4c8593b37be2ea6b2db0a5488a88ac0000000001000000046cae2ea01aa73bb487363e3b9f5ced25c5ec7c3f9f2afa681d69e5f702722514030000006b483045022100b70347bfeda52f523c330e71d9c9692796c234be1ae1a4511ca8b4a4a9e40e7902201974c8a4d97c1adc39cb67fa96467da07dfec6a40d176a1f4ab9062716490f7c0121032b1426ddfb603bd991bc678c4e0c37749a8e1fc9d9fd49ab52acf21c11169ae0ffffffff729d416c3140840e3ab6ef7113aa6af6093c0b80838ee2d1b6c9746804668846040000006b483045022100b6b2aea6928972888ca52f1be4429e0dff4fc68e698df8a874eed9282cefb93702203a62197f0d51ec69f1bd9726823840e908d02ee51aba09232273f1eed9baf92d0121032b1426ddfb603bd991bc678c4e0c37749a8e1fc9d9fd49ab52acf21c11169ae0ffffffff5b2e8a98ea4c44bd45f5ecfdda08f8900206fcb24a150a0ac71e58a54513eac3060000006b483045022100fd9a947e137b8e6560e3d45cf8dc3b7e2ccc665e2395144a51d5b2eac817e91502204f7a33c876b810643adb334fbdc38d3da03ae6482dfb71e7b5302e75df2c63270121032b1426ddfb603bd991bc678c4e0c37749a8e1fc9d9fd49ab52acf21c11169ae0ffffffffbdecce9e49f8fd67ddb5ca8bb075876959ff3d5e5f92988b56c4a3e0308b5bd7020000006b483045022100bf09481f83ac92fefc84106b0da6ca857cdbccda922cc6f96124f9c94d2158b802200a943d992064f50c90e2a25d1ba9a75eac6f081deaadd21da09f814c4a59b42b0121032b1426ddfb603bd991bc678c4e0c37749a8e1fc9d9fd49ab52acf21c11169ae0ffffffff02c0a490f8070000001976a9145005bf8108fa09fe20266fd48a9ff7f6c19158d488acc0f9a8192e0000001976a9144aac33bb4bde57ca085765f8e83cbf75fca0526888ac000000003d3eee63aac3cac953a8d954d5d4ec7b3933ab5a163d3da5a023a4c3d3de0bb996721715a86159a0c14d2638dcfd0de9d7746aec700fff699e304c720c90f6760045c494868d6f35bde104d7528a3584719d19179a67f1278eb03d58b04593d509327655f71e92ddf6d861c6c67ded22c6f63fbf143939c7a80f4cac2dec690d6c"));
        FilteredBlock filteredBlock = new FilteredBlock(UNITTEST, HEX.decode("010100006e8e7985c769d3570b2f6e81798b93e9facc9e60c7c7ab503d5fa9e995a51de4017abb56aa9bf8bc411570c73b4b90e1c602d172291e3f08f2a9ce9dd6ce1e24d81f9653d5daf180efff35c7e9c155c28b509158ea264e065e06c435148e31ee027d4f5a66519e840c0000000858cac1b66a2b81f97c3aacbae7130adb8fc4856e2f43dc19284dbb36d4e98b84ee3a4f7a972297628deb24a798b203a351363448a9e78a7b85aa491bd2256a75bc4d4443229d7f4af86ab5ba42efe2d9662f216f006facfe63c43cffcc519744b2a909946713aca7922fc5af196d846287dbe27585234d36940ac56962a5a935a00fdbd82bfd1cfbfbc0ab9b5a0326e6b930d9d51004abc9f244eabf759db3419d8482dbc303bc1a2b210e44cff516ce18aaf2cc385de92f2c01e90bea7d81874d25bacce7de0f11f8e60394fd2745a50bd3ca3ff607a45a327c7286e29e9bfa6cf8e8dfb2ae9a357ae09fb0927f4949d73dc1f5e7c54541b6c5cafd8b6a7a9c02ff1a"));
        
        // Block 81891
        assertTrue(block.getHash().equals(Sha256Hash.wrap("d2152f905bbf6d818a8ad6918fde778f875c03dc75606798704c925c50c14368")));
        assertTrue(filteredBlock.getHash().equals(block.getHash()));
        
        List<Sha256Hash> txHashList = filteredBlock.getTransactionHashes();
        assertTrue(txHashList.size() == 4);
        // Four transactions (0, 1, 2, 6) from block 81891
        Transaction tx0 = UNITTEST.getDefaultSerializer().makeTransaction(HEX.decode("01000000010000000000000000000000000000000000000000000000000000000000000000ffffffff0703e33f01025c14ffffffff01c0d8a700000000001976a9140fb3107609cb2e68fefc4236817b5db703ad9a9a88ac00000000"));
        assertTrue(tx0.getTxId().equals(Sha256Hash.wrap("848be9d436bb4d2819dc432f6e85c48fdb0a13e7baac3a7cf9812b6ab6c1ca58")));
        assertEquals(tx0.getTxId(), txHashList.get(0));
        
        Transaction tx1 = UNITTEST.getDefaultSerializer().makeTransaction(HEX.decode("0100000001ecf65f112e2d28c2b47b25d52719c73be8bcc949241a166f1cd391c18019ada4010000006a47304402200de99e40748a93f506ed6780b714b0124df466bbe6bf8141819a856773dca9ff022053891a57565a8948d7047c231f92e3d69563e447ce67ab7103e3f59b6c52b3a40121028e624b9dbb164d7e930f45b4b064050890972b9118605325298deb53babf2baaffffffff01c0312ca40b0000001976a9141aec0f7b7bbd85dd8c9d13594c3e9346521ef49388ac00000000"));
        assertTrue(tx1.getTxId().equals(Sha256Hash.wrap("756a25d21b49aa857b8ae7a948343651a303b298a724eb8d629722977a4f3aee")));
        assertEquals(tx1.getTxId(), txHashList.get(1));

        Transaction tx2 = UNITTEST.getDefaultSerializer().makeTransaction(HEX.decode("0100000001796daca022e9d274f4064b0ecc533d69fe2bba86c118a82aa7263cdac0737d10010000006a47304402200144864809c861eb2af4ee256d89afe4ec0ebb77f14a1851dadda6bc6f649bc50220224e4074593dafc8fef6d30cca992713353b7d7d6259717ee6597907b6c46706012103c7c8b6629e3167c5497ec9120fcb450c294180ae98052c37150802989acbcd70ffffffff01c0512677000000001976a914827faf94ba53d5c38c200fe274873644f3ce4bb688ac00000000"));
        assertTrue(tx2.getTxId().equals(Sha256Hash.wrap("449751ccff3cc463feac6f006f212f66d9e2ef42bab56af84a7f9d2243444dbc")));
        assertEquals(tx2.getTxId(), txHashList.get(2));


        Transaction tx3 = UNITTEST.getDefaultSerializer().makeTransaction(HEX.decode("01000000012b800a005ee45a61da805c564735fa8f4b0ecbe9d57ca40910a8e8c08573e34a000000006a47304402207e338195ab25c9ce2ffdb5eaa8e3f5a2617e91b7c67454f326d75958ebb2f90c022076f05eb463a312a773eb2daa21f81107a8a31204b9caa4f85886c7bdf229bd2c01210369279daaa48172ed6443127003c3bf9522427efc083faac6cb427e7614ea32e6ffffffff02c09ee605000000001976a914750ba28e54682792ca4c5770fc29beda9baff0bf88acc0f66b12020000001976a914d0290f0c574b553f0937a4e1615be43ebdeea77e88ac00000000"));
        assertTrue(tx3.getTxId().equals(Sha256Hash.wrap("87817dea0be9012c2fe95d38ccf2aa18ce16f5cf440e212b1abc03c3db82849d")));
        assertEquals(tx3.getTxId(),txHashList.get(3));

        BloomFilter filter = wallet.getBloomFilter(wallet.getKeyChainGroupSize()*2, 0.001, 0xDEADBEEF);
        // Compare the serialized bloom filter to a known-good value
        assertArrayEquals(HEX.decode("01ff32000000efbeadde02"), filter.unsafeBitcoinSerialize());

        // Create a peer.
        peerGroup.start();
        InboundMessageQueuer p1 = connectPeer(1);
        assertEquals(1, peerGroup.numConnectedPeers());
        // Send an inv for block 81891
        InventoryMessage inv = new InventoryMessage(UNITTEST);
        inv.addBlock(block);
        inbound(p1, inv);
        
        // Check that we properly requested the correct FilteredBlock
        Object getData = outbound(p1);
        assertTrue(getData instanceof GetDataMessage);
        assertTrue(((GetDataMessage)getData).getItems().size() == 1);
        assertTrue(((GetDataMessage)getData).getItems().get(0).hash.equals(block.getHash()));
        assertTrue(((GetDataMessage)getData).getItems().get(0).type == InventoryItem.Type.FILTERED_BLOCK);
        
        // Check that we then immediately pinged.
        Object ping = outbound(p1);
        assertTrue(ping instanceof Ping);
        
        // Respond with transactions and the filtered block
        inbound(p1, filteredBlock);
        inbound(p1, tx0);
        inbound(p1, tx1);
        inbound(p1, tx2);
        inbound(p1, tx3);
        inbound(p1, new Pong(((Ping)ping).getNonce()));

        pingAndWait(p1);

        Set<Transaction> transactions = wallet.getTransactions(false);
        assertTrue(transactions.size() == 4);
        for (Transaction tx : transactions) {
            assertTrue(tx.getConfidence().getConfidenceType() == ConfidenceType.BUILDING);
            assertTrue(tx.getConfidence().getDepthInBlocks() == 1);
            assertTrue(tx.getAppearsInHashes().keySet().contains(block.getHash()));
            assertTrue(tx.getAppearsInHashes().size() == 1);
        }

        // Peer 1 goes away.
        closePeer(peerOf(p1));
    }

    @Test
    public void parseHugeDeclaredSizePartialMerkleTree() throws Exception{
        final byte[] bits = new byte[1];
        bits[0] = 0x3f;
        final List<Sha256Hash> hashes = new ArrayList<>();
        hashes.add(Sha256Hash.wrap("0000000000000000000000000000000000000000000000000000000000000001"));
        hashes.add(Sha256Hash.wrap("0000000000000000000000000000000000000000000000000000000000000002"));
        hashes.add(Sha256Hash.wrap("0000000000000000000000000000000000000000000000000000000000000003"));
        PartialMerkleTree pmt = new PartialMerkleTree(UNITTEST, bits, hashes, 3) {
            public void bitcoinSerializeToStream(OutputStream stream) throws IOException {
                uint32ToByteStreamLE(getTransactionCount(), stream);
                // Add Integer.MAX_VALUE instead of hashes.size()
                stream.write(new VarInt(Integer.MAX_VALUE).encode());
                //stream.write(new VarInt(hashes.size()).encode());
                for (Sha256Hash hash : hashes)
                    stream.write(hash.getReversedBytes());

                stream.write(new VarInt(bits.length).encode());
                stream.write(bits);
            }
        };
        byte[] serializedPmt = pmt.bitcoinSerialize();
        try {
            new PartialMerkleTree(UNITTEST, serializedPmt, 0);
            fail("We expect ProtocolException with the fixed code and OutOfMemoryError with the buggy code, so this is weird");
        } catch (ProtocolException e) {
            //Expected, do nothing
        }
    }
}
