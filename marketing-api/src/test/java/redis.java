import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.common.util.AESAlgorithmUtil;
import com.br.marketing.api.MarketingApiApplication;
import com.br.marketing.origin.CaffeineCache;
import com.br.marketing.rpcclient.rpcclientImpl.DecodeGrpcClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.intelligentcustomerservice.IntelligentCustomerServiceClient;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailVariablesDTO;
import com.br.marketing.common.utils.*;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.entity.*;
import com.br.marketing.es.service.MarketingHistoryEsService;
import com.br.marketing.es.util.BrCipherMaker;
import com.br.marketing.mapper.*;
import com.br.marketing.monkeydata.entity.commonobj.PageCondition;
import com.br.marketing.monkeydata.handle.IMonkeyDataHandle;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.vo.CustGroupTempVO;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.shaded.com.google.common.base.Splitter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MarketingApiApplication.class)
@Slf4j
public class redis {

    @Autowired
    RabbitMqProducter producter;

    @Resource
    CaffeineCache caffeineCache;

    @Test
    public void testPriority() throws InterruptedException {
        CustomerRoutingKeyConfig routingKeyConfig = caffeineCache.getRountingKey("3710058,6");
        CustomerRoutingKeyConfig routingKeyConfig2 = caffeineCache.getRountingKey("3710058,1");
        CustomerRoutingKeyConfig routingKeyConfig1 = caffeineCache.getRountingKey("3710078,1");
        for (int i = 0; i <20;i++) {
            producter.send("Marketing.PreUser.Receive.Small",String.valueOf(i),i);
        }
        for (int i = 0; i <30;i++) {
            producter.send("Marketing.PreUser.Receive.Emergency",String.valueOf(i),i);
        }
        for (int i = 0; i <30;i++) {
            producter.send("Marketing.PreUser.Receive",String.valueOf(i),i);
        }
    }

    @Test
    public void pushMQ(){
        String a = "166014,166015,166016,166017,166018,166019,166020,166021,166022,166023,166024,166025,166026,166027,166028,166029,166030,166031,166032,166033,166034,166035,166036,166037,166038,166039,166040,166041,166042,166043,166044,166045,166046,166047,166048,166049,166050,166051,166052,166053,166054,166055,166056,166057,166058,166059,166060,166061,166062,166063,166064,166065,166066,166067,166068,166069,166070,166071,166072,166073,166074,166075,166076,166077,166078,166079,166080,166081,166082,166083,166084,166085,166086,166087,166088,166089,166090,166091,166092,166093,166094,166095,166096,166097,166098,166099,166100,166101,166102,166103,166104,166105,166106,166107,166108,166109,166110,166111,166112,166113,166114,166115,166116,166117,166118,166119,166120,166121,166122,166123,166124,166125,166126,166127,166128,166129,166130,166131,166132,166133,166134,166135,166136,166137,166138,166139,166140,166141,166142,166143,166144,166145,166146,166147,166148,166149,166150,166151,166152,166153,166154,166155,166156,166157,166158,166159,166160,166161,166162,166163,166164,166165,166166,166167,166168,166169,166170,166171,166172,166173,166174,166175,166176,166177,166178,166179,166180,166181,166182,166183,166184,166185,166186,166187,166188,166189,166190,166191,166192,166193,166194,166195,166196,166197,166198,166199,166200,166201,166202,166203,166204,166205,166206,166207,166208,166209,166210,166211,166212,166213,166214,166215,166216,166217,166218,166219,166220,166221,166222,166223,166224,166225,166226,166227,166228,166229,166230,166231,166232,166233,166234,166235,166236,166237,166238,166239,166240,166241,166242,166243,166244,166245,166246,166247,166248,166249,166250,166251,166252,166253,166254,166255,166256,166257,166258,166259,166260,166261,166262,166263,166264,166265,166266,166267,166268,166269,166270,166271,166272,166273,166274,166275,166276,166277,166278,166279,166280,166281,166282,166283,166284,166285,166286,166287,166288,166289,166290,166291,166292,166293,166294,166295,166296,166297,166298,166299,166300,166301,166302,166303,166304,166305,166306,166307,166308,166309,166310,166311,166312,166313,166314,166315,166316,166317,166318,166319,166320,166321,166322,166323,166324,166325,166326,166327,166328,166329,166330,166331,166332,166333,166334,166335,166336,166337,166338,166339,166340,166341,166342,166343,166344,166345,166346,166347,166348,166349,166350,166351,166352,166353,166354,166355,166356,166357,166358,166359,166360,166361,166362,166363,166364,166365,166366,166367,166368,166369,166370,166371,166372,166373,166374,166375,166376,166377,166378,166379,166380,166381,166382,166383,166384,166385,166386,166387,166388,166389,166390,166391,166392,166393,166394,166395,166396,166397,166398,166399,166400,166401,166402,166403,166404,166405,166406,166407,166408,166409,166410,166411,166412,166413,166414,166415,166416,166417,166418,166419,166420,166421,166422,166423,166424,166425,166426,166427,166428,166429,166430,166431,166432,166433,166434,166435,166436,166437,166438,166439,166440,166441,166442,166443,166444,166445,166446,166447,166448,166449,166450,166451,166452,166453,166454,166455,166456,166457,166458,166459,166460,166461,166462,166463,166464,166465,166466,166467,166468,166469,166470,166471,166472,166473,166474,166475,166476,166477,166478,166479,166480,166481,166482,166483,166484,166485,166486,166487,166488,166489,166490,166491,166492,166493,166494,166495,166496,166497,166498,166499,166500,166501,166502,166503,166504,166505,166506,166507,166508,166509,166510,166511,166512,166513,166514,166515,166516,166517,166518,166519,166520,166521,166522,166523,166524,166525,166526,166527,166528,166529,166530,166531,166532,166533,166534,166535,166536,166537,166538,166539,166540,166541,166542,166543,166544,166545,166546,166547,166548,166549,166550,166551,166552,166553,166554,166555,166556,166557,166558,166559,166560,166561,166562,166563,166564,166565,166566,166567,166568,166569,166570,166571,166572,166573,166574,166575,166576,166577,166578,166579,166580,166581,166582,166583,166584,166585,166586,166587,166588,166589,166590,166591,166592,166593,166594,166595,166596,166597,166598,166599,166600,166601,166602,166603,166604,166605,166606,166607,166608,166609,166610,166611,166612,166613,166614,166615,166616,166617,166618,166619,166620,166621,166622,166623,166624,166625,166626,166627,166628,166629,166630,166631,166632,166633,166634,166635,166636,166637,166638,166639,166640,166641,166642,166643,166644,166645,166646,166647,166648,166649,166650,166651,166652,166653,166654,166655,166656,166657,166658,166659,166660,166661,166662,166663,166664,166665,166666,166667,166668,166669,166670,166671,166672,166673,166674,166675,166676,166677,166678,166679,166680,166681,166682,166683,166684,166685,166686,166687,166688,166689,166690,166691,166692,166693,166694,166695,166696,166697,166698,166699,166700,166701,166702,166703,166704,166705,166706,166707,166708,166709,166710,166711,166712,166713,166714,166715,166716,166717,166718,166719,166720,166721,166722,166723,166724,166725,166726,166727,166728,166729,166730,166731,166732,166733,166734,166735,166736,166737,166738,166739,166740,166741,166742,166743,166744,166745,166746,166747,166748,166749,166750,166751,166752,166753,166754,166755,166756,166757,166758,166759,166760,166761,166762,166763,166764,166765,166766,166767,166768,166769,166770,166771,166772,166773,166774,166775,166776,166777,166778,166779,166780,166781,166782,166783,166784,166785,166786,166787,166788,166789,166790,166791,166792,166793,166794,166795,166796,166797,166798,166799,166800,166801,166802,166803,166804,166805,166806,166807,166808,166809,166810,166811,166812,166813,166814,166815,166816,166817,166818,166819,166820,166821,166822,166823,166824,166825,166826,166827,166828,166829,166830,166831,166832,166833,166834,166835,166836,166837,166838,166839,166840,166841,166842,166843,166844,166845,166846,166847,166848,166849,166850,166851,166852,166853,166854,166855,166856,166857,166858,166859,166860,166861,166862,166863,166864,166865,166866,166867,166868,166869,166870,166871,166872,166873,166874,166875,166876,166877,166878,166879,166880,166881,166882,166883,166884,166885,166886,166887,166888,166889,166890,166891,166892,166893,166894,166895,166896,166897,166898,166899,166900,166901,166902,166903,166904,166905,166906,166907,166908,166909,166910,166911,166912,166913,166914,166915,166916,166917,166918,166919,166920,166921,166922,166923,166924,166925,166926,166927,166928,166929,166930,166931,166932,166933,166934,166935,166936,166937,166938,166939,166940,166941,166942,166943,166944,166945,166946,166947,166948,166949,166950,166951,166952,166953,166954,166955,166956,166957,166958,166959,166960,166961,166962,166963,166964,166965,166966,166967,166968,166969,166970,166971,166972,166973,166974,166975,166976,166977,166978,166979,166980,166981,166982,166983,166984,166985,166986,166987,166988,166989,166990,166991,166992,166993,166994,166995,166996,166997,166998,166999,167000,167001,167002,167003,167004,167005,167006,167007,167008,167009,167010,167011,167012,167013,167014,167015,167016,167017,167018,167019,167020,167021,167022,167023,167024,167025,167026,167027,167028,167029,167030,167031,167032,167033,167034,167035,167036,167037,167038,167039,167040,167041,167042,167043,167044,167045,167046,167047,167048,167049,167050,167051,167052,167053,167054,167055,167056,167057,167058,167059,167060,167061,167062,167063,167064,167065,167066,167067,167068,167069,167070,167071,167072,167073,167074,167075,167076,167077,167078,167079,167080,167081,167082,167083,167084,167085,167086,167087,167088,167089,167090,167091,167092,167093,167094,167095,167096,167097,167098,167099,167100,167101,167102,167103,167104,167105,167106,167107,167108,167109,167110,167111,167112,167113,167114,167115,167116,167117,167118,167119,167120,167121,167122,167123,167124,167125,167126,167127,167128,167129,167130,167131,167132,167133,167134,167135,167136,167137,167138,167139,167140,167141,167142,167143,167144,167145,167146,167147,167148,167149,167150,167151,167152,167153,167154,167155,167156,167157,167158,167159,167160,167161,167162,167163,167164,167165,167166,167167,167168,167169,167170,167171,167172,167173,167174,167175,167176,167177,167178,167179,167180,167181,167182,167183,167184,167185,167186,167187,167188,167189,167190,167191,167192,167193,167194,167195,167196,167197,167198,167199,167200,167201,167202,167203,167204,167205,167206,167207,167208,167209,167210,167211,167212,167213,167214,167215,167216,167217,167218,167219,167220,167221,167222,167223,167224,167225,167226,167227,167228,167229,167230,167231,167232,167233,167234,167235,167236,167237,167238,167239,167240,167241,167242,167243,167244,167245,167246,167247,167248,167249,167250,167251,167252,167253,167254,167255,167256,167257,167258,167259,167260,167261,167262,167263,167264,167265,167266,167267,167268,167269,167270,167271,167272,167273,167274,167275,167276,167277,167278,167279,167280,167281,167282,167283,167284,167285,167286,167287,167288,167289,167290,167291,167292,167293,167294,167295,167296,167297,167298,167299,167300,167301,167302,167303,167304,167305,167306,167307,167308,167309,167310,167311,167312,167313,167314,167315,167316,167317,167318,167319,167320,167321,167322,167323,167324,167325,167326,167327,167328,167329,167330,167331,167332,167333,167334,167335,167336,167337,167338,167339,167340,167341,167342,167343,167344,167345,167346,167347,167348,167349,167350,167351,167352,167353,167354,167355,167356,167357,167358,167359,167360,167361,167362,167363,167364,167365,167366,167367,167368,167369,167370,167371,167372,167373,167374,167375,167376,167377,167378,167379,167380,167381,167382,167383,167384,167385,167386,167387,167388,167389,167390,167391,167392,167393,167394,167395,167396,167397,167398,167399,167400,167401,167402,167403,167404,167405,167406,167407,167408,167409,167410,167411,167412,167413,167414,167415,167416,167417,167418,167419,167420,167421,167422,167423,167424,167425,167426,167427,167428,167429,167430,167431,167432,167433,167434,167435,167436,167437,167438,167439,167440,167441,167442,167443,167444,167445,167446,167447,167448,167449,167450,167451,167452,167453,167454,167455,167456,167457,167458,167459,167460,167461,167462,167463,167464,167465,167466,167467,167468,167469,167470,167471,167472,167473,167474,167475,167476,167477,167478,167479,167480,167481,167482,167483,167484,167485,167486,167487,167488,167489,167490,167491,167492,167493,167494,167495,167496,167497,167498,167499,167500,167501,167502,167503,167504,167505,167506,167507,167508,167509,167510,167511,167512,167513,167514,167515,167516,167517,167518,167519,167520,167521,167522,167523,167524,167525,167526,167527,167528,167529,167530,167531,167532,167533,167534,167535,167536,167537,167538,167539,167540,167541,167542,167543,167544,167545,167546,167547,167548,167549,167550,167551,167552,167553,167554,167555,167556,167557,167558,167559,167560,167561,167562,167563,167564,167565,167566,167567,167568,167569,167570,167571,167572,167573,167574,167575,167576,167577,167578,167579,167580,167581,167582,167583,167584,167585,167586,167587,167588,167589,167590,167591,167592,167593,167594,167595,167596,167597,167598,167599,167600,167601,167602,167603,167604,167605,167606,167607,167608,167609,167610,167611,167612,167613,167614,167615,167616,167617,167618,167619,167620,167621,167622,167623,167624,167625,167626,167627,167628,167629,167630,167631,167632,167633,167634,167635,167636,167637,167638,167639,167640,167641,167642,167643,167644,167645,167646,167647,167648,167649,167650,167651,167652,167653,167654,167655,167656,167657,167658,167659,167660,167661,167662,167663,167664,167665,167666,167667,167668,167669,167670,167671,167672,167673,167674,167675,167676,167677,167678,167679,167680,167681,167682,167683,167684,167685,167686,167687,167688,167689,167690,167691,167692,167693,167694,167695,167696,167697,167698,167699,167700,167701,167702,167703,167704,167705,167706,167707,167708,167709,167710,167711,167712,167713,167714,167715,167716,167717,167718,167719,167720,167721,167722,167723,167724,167725,167726,167727,167728,167729,167730,167731,167732,167733,167734,167735,167736,167737,167738,167739,167740,167741,167742,167743,167744,167745,167746,167747,167748,167749,167750,167751,167752,167753,167754,167755,167756,167757,167758,167759,167760,167761,167762,167763,167764,167765,167766,167767,167768,167769,167770,167771,167772,167773,167774,167775,167776,167777,167778,167779,167780,167781,167782,167783,167784,167785,167786,167787,167788,167789,167790,167791,167792,167793,167794,167795,167796,167797,167798,167799,167800,167801,167802,167803,167804,167805,167806,167807,167808,167809,167810,167811,167812,167813,167814,167815,167816,167817,167818,167819,167820,167821,167822,167823,167824,167825,167826,167827,167828,167829,167830,167831,167832,167833,167834,167835,167836,167837,167838,167839,167840,167841,167842,167843,167844,167845,167846,167847,167848,167849,167850,167851,167852,167853,167854,167855,167856,167857,167858,167859,167860,167861,167862,167863,167864,167865,167866,167867,167868,167869,167870,167871,167872,167873,167874,167875,167876,167877,167878,167879,167880,167881,167882,167883,167884,167885,167886,167887,167888,167889,167890,167891,167892,167893,167894,167895,167896,167897,167898,167899,167900,167901,167902,167903,167904,167905,167906,167907,167908,167909,167910,167911,167912,167913,167914,167915,167916,167917,167918,167919,167920,167921,167922,167923,167924,167925,167926,167927,167928,167929,167930,167931,167932,167933,167934,167935,167936,167937,167938,167939,167940,167941,167942,167943,167944,167945,167946,167947,167948,167949,167950,167951,167952,167953,167954,167955,167956,167957,167958,167959,167960,167961,167962,167963,167964,167965,167966,167967,167968,167969,167970,167971,167972,167973,167974,167975,167976,167977,167978,167979,167980,167981,167982,167983,167984,167985,167986,167987,167988,167989,167990,167991,167992,167993,167994,167995,167996,167997,167998,167999,168000,168001,168002,168003,168004,168005,168006,168007,168008,168009,168010,168011,168012,168013";
        String[] split = a.split(",");
        for (String s : split) {
            producter.send("Marketing.PreUser.Receive",s);
        }
    }

//

    @Autowired
    IMonkeyDataHandle zhongAnHandleImpl;

    @Test
    public void testClean(){
        PageCondition pageCondition = new PageCondition();
        pageCondition.setPageIndex(1);
        zhongAnHandleImpl.action(pageCondition);
    }

    @Test
    public void testAes(){
        String encrypt = AESAlgorithmUtil.encrypt("yPcxugMgQChJovqtGfqFUuBUA==", Constants.SFTP_P_SECRET_KEY);
        String encrypt1 = AESAlgorithmUtil.encrypt("PCIboXXw2+JeAozVbl1lxsG", Constants.SFTP_P_SECRET_KEY);
        System.out.println("密文"+encrypt);
        System.out.println("密文1"+encrypt1);
    }
    @Test
    public void test(){
       RpcClientProxy.sendUploadLog("123");
    }

    @Autowired
    RedisChgService redisChgService;

    @Autowired
    StraHisFileMapper straHisFileMapper;

    @Autowired
    IntelligentCustomerServiceClient intelligentCustomerServiceClient;

    @Test
    public void testPushUser(){
        String s = DigestUtils.md5DigestAsHex(BrCipherMaker.getInstance().decode("AgsNΒ7VlVSWwkAVwY").getBytes());
        String s2 = DigestUtils.md5DigestAsHex(BrCipherMaker.getInstance().decode("Uw0JDAoΒ6DBVRdXF0").getBytes());
        System.out.println(s);
        System.out.println(s2);
        String ss = BrCipherMaker.getInstance().decode("UwAKCQAFCVBXV1Β6M");
        System.out.println(ss);
//        String ab = "{\"apiCode\":\"7410438\",\"jsonData\":{\"accessNumber\":\"123123_2\",\"batchNumber\":\"123123\",\"data\":[{\"caseNumber\":\"1_82021072601_csd_1627293995809\",\"phone\":\"AgsNΒ7VlVSWwkAVwY\",\"variables\":{\"groupType\":\"促首登\",\"score\":\"83.0\",\"scoreDate\":\"2021-07-26\",\"scoreName\":\"scorencashonshcdlyxf\",\"taskId\":\"82021072601\",\"update\":\"\"}}],\"extendData\":{\"sampleTotal\":\"1\",\"scoreName\":\"scorencashonshcdlyxf\"},\"method\":\"caseAdd\"},\"platApiCode\":\"7410438\"}";
//        PushMarketingUserDTO o = JSON.parseObject(ab, new TypeReference<PushMarketingUserDTO>() {
//        }.getType());
//        Result<Integer> integerResult = intelligentCustomerServiceClient.pushUser(o, 123L, "123");
//        System.out.println(integerResult.getMessage());
    }

    @Test
    public void testRedis(){
        String s = redisChgService.get("acb:");
        boolean notBlank = StringUtils.isNotBlank(s);
        boolean notBlank2 = StringUtils.isNotBlank(null);
        boolean notBlank1 = StringUtils.isNotBlank("");
        System.out.println("test");
    }

    @Test
    public void serTest(){
        PushMarketingUserDetailVariablesDTO pushMarketingUserDetailVariablesDTO = new PushMarketingUserDetailVariablesDTO();
        pushMarketingUserDetailVariablesDTO.setScore("123");
        pushMarketingUserDetailVariablesDTO.setScoreDate("123");
        pushMarketingUserDetailVariablesDTO.setScoreName("123");
        pushMarketingUserDetailVariablesDTO.setUpdate("123");
        System.out.println(JSON.toJSONString(pushMarketingUserDetailVariablesDTO));
        List<String> strList = new ArrayList<>();
        strList.add("123");
        strList.add("456");
        strList.add("789");
        here: for (String s : strList) {

        }
    }

    @Test
    public void testPool(){
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(10, 10);
        ThreadPoolExecutor threadPool2 = BrExecutors.getThreadPool(10, 10);
        for (int i = 0; i < 10; i++) {
            threadPool.submit(()->{System.out.println("threadPool==="+Thread.currentThread().getName()+Thread.currentThread().getId());});
            threadPool2.submit(()->{System.out.println("threadPool2==="+Thread.currentThread().getName()+Thread.currentThread().getId());});
        }
    }

    @Test
    public void testDes(){
        String s = "{\"taskId\":\"4ec94c9e2a61a05bf912a3b9f9684f1a\",\"dataItems\":[{\"cell\":\"67c3461a3ef7be453775cf9227fa60db\",\"groupType\":\"促首登\",\"caseNum\":\"1\",\"registerDate\":\"2021-06-04\",\"reserveField1\":\"1\",\"reserveField2\":\"1\"},{\"cell\":\"e01d4dc231b25fef9672b43408c4a496\",\"groupType\":\"促首登\",\"caseNum\":\"2\",\"registerDate\":\"2021-06-04\",\"reserveField1\":\"2\",\"reserveField2\":\"2\"},{\"cell\":\"d258d79465755613dc28fe28b71b13cd\",\"groupType\":\"促首登\",\"caseNum\":\"3\",\"registerDate\":\"2021-06-04\",\"reserveField1\":\"3\",\"reserveField2\":\"3\"},{\"cell\":\"feeba7577a2de1521619c4629b4123c1\",\"groupType\":\"促首登\",\"caseNum\":\"4\",\"registerDate\":\"2021-06-04\",\"reserveField1\":\"4\",\"reserveField2\":\"4\"},{\"cell\":\"a3627d465c9466abfe78ea8695733c27\",\"groupType\":\"促首登\",\"caseNum\":\"5\",\"registerDate\":\"2021-06-04\",\"reserveField1\":\"5\",\"reserveField2\":\"5\"},{\"cell\":\"6f86c136018169210d813093c0218fb8\",\"groupType\":\"促首登\",\"caseNum\":\"6\",\"registerDate\":\"2021-06-04\",\"reserveField1\":\"6\",\"reserveField2\":\"6\"},{\"cell\":\"5d11596a5966d1344ec7738ea0956256\",\"groupType\":\"促首登\",\"caseNum\":\"7\",\"registerDate\":\"2021-06-04\",\"reserveField1\":\"7\",\"reserveField2\":\"7\"},{\"cell\":\"368f5fe5a8544afe18745f1297a3f02d\",\"groupType\":\"促首登\",\"caseNum\":\"8\",\"registerDate\":\"2021-06-04\",\"reserveField1\":\"8\",\"reserveField2\":\"8\"},{\"cell\":\"2c284c58471060b8f61b338619ace49d\",\"groupType\":\"促首登\",\"caseNum\":\"9\",\"registerDate\":\"2021-06-04\",\"reserveField1\":\"9\",\"reserveField2\":\"9\"},{\"cell\":\"f4eae314a456eb2e49bff509131f52af\",\"groupType\":\"促首登\",\"caseNum\":\"10\",\"registerDate\":\"2021-06-04\",\"reserveField1\":\"10\",\"reserveField2\":\"10\"},{\"cell\":\"28ad5e6d5c0ab384634c17e465b23371\",\"groupType\":\"促首登\",\"caseNum\":\"11\",\"registerDate\":\"2021-06-04\",\"reserveField1\":\"11\",\"reserveField2\":\"11\"},{\"cell\":\"2874e8d6c8a24a76b3b90ff041e50a0a\",\"groupType\":\"促首登\",\"caseNum\":\"12\",\"registerDate\":\"2021-06-04\",\"reserveField1\":\"12\",\"reserveField2\":\"12\"},{\"cell\":\"9945ac42bad09b576048b03918a5f090\",\"groupType\":\"促首登\",\"caseNum\":\"13\",\"registerDate\":\"2021-06-04\",\"reserveField1\":\"13\",\"reserveField2\":\"13\"},{\"cell\":\"1a3d04d493e6554b5148907d36057b11\",\"groupType\":\"促首登\",\"caseNum\":\"14\",\"registerDate\":\"2021-06-04\",\"reserveField1\":\"14\",\"reserveField2\":\"14\"},{\"cell\":\"8a034fa1866f256cc589af717a4a705c\",\"groupType\":\"促首登\",\"caseNum\":\"15\",\"registerDate\":\"2021-06-04\",\"reserveField1\":\"15\",\"reserveField2\":\"15\"}]}";
        MarketingPreUserDTO o = JSON.parseObject(s, new TypeReference<MarketingPreUserDTO>() {
        }.getType());
        System.out.println(o.toString());
    }

    @Test
    public void testThread(){
        ExecutorService threadPoolExecutor = new ThreadPoolExecutor(30, 30,60L,TimeUnit.SECONDS
                ,new ArrayBlockingQueue(200),new ThreadFactoryBuilder().setNameFormat("br-test-pool-%d").build()
                , new ThreadPoolExecutor.CallerRunsPolicy());
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            final int a = i;
            threadPoolExecutor.submit(()->{
                try {
                    Thread.sleep(200L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String name = Thread.currentThread().getName();
                System.out.println(name.concat(":").concat(String.valueOf(a)));
            });
        }
        threadPoolExecutor.shutdown();
        Boolean b = true;
        while (b){
            if(threadPoolExecutor.isTerminated()){
                System.out.println("结束");
                b=false;
            }else{
                System.out.println("休息");
                try {
                    Thread.sleep(3000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("结束:".concat(String.valueOf(end-start)));

    }

    @Test
    public void randomTest(){
        String s = RandomUtils.randomStr(2);
        String s1 = RandomUtils.randomStr(2);
        String s2 = RandomUtils.randomStr(2);
        System.out.println(s+"__"+s1+"__"+s2);

    }

    @Test
    public void testConcurrent(){
        Integer k=10;
        List<String> list =new ArrayList();
        long start = System.currentTimeMillis();
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        List<Callable<List>> callableList = new ArrayList<>();
        for (Integer i = 0; i < k; i++) {
            callableList.add(()->{
                List list1 = new ArrayList();
                Integer kk = 20000;
                for (Integer integer = 0; integer < kk; integer++) {
                    list1.add("kk".concat(String.valueOf(kk)));
                }
                return list1;
            });
        }
        try {
            List<Future<List>> futures = executorService.invokeAll(callableList);
            futures.forEach(t->{
                try {
                    list.addAll(t.get());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(list.size()+"_________"+(System.currentTimeMillis()-start));

        long start2 = System.currentTimeMillis();
        Integer kkk=200000;
        List<String> list2 =new ArrayList();
        for (Integer i = 0; i < kkk; i++) {
            list2.add("kkk".concat(String.valueOf(kkk)));
        }
        System.out.println(list2.size()+"_________"+(System.currentTimeMillis()-start2));
        System.out.println("1231");



    }

    @Test
    public void encAnddec() throws Exception {
        String s = ThreeDes.encryptByCBC("123", "abcddesds", "abcdefgh");
        String abcddesds = ThreeDes.decryptByCBC(s, "abcddesds","hhhhtttt");
        System.out.println(abcddesds);
    }

    @Test
    public void indexTest(){
        String s = "acd-acd-acd-acd";
        int acd = s.indexOf("acd");
        System.out.println(acd);
        String s1 = "abd-acd-acd-acd";
        int acd1 = s1.indexOf("acd");
        System.out.println(acd1);
        int abd = s.indexOf("abd");
        System.out.println(abd);
    }

    @Autowired
    MarketingHistoryEsService marketingHistoryEsService;

    @Autowired
    MarketingTaskMapper marketingTaskMapper;

    @Autowired
    MarketingStrategyProductMapper productMapper;

    DecimalFormat df = new DecimalFormat("######0.000");
    @Test
    public void testScript(){
//        Date date = new Date();
//        String apiCode = "7410431";
//        for (int k = 0; k < 10; k++) {
//            final Integer m = k;
//            new Thread(()->{
//            //region 跑测试数据
//            double scoreA = 0.001;
//            double scoreB = 0.001;
//
//            String number = "7410431_20210621172100_yp_test6_" + String.valueOf(m);
//            StraHisFile file = new StraHisFile();
//            file.setApiCode(apiCode);
//            file.setBatchNumber(number);
//            Date date1 = new Date();
//            file.setCreateTime(date1);
//            file.setUpdateTime(date1);
//            file.setStatus(2);
//            file.setZipStatus(1);
//            file.setScoreStatus(2);
//            file.setFilePath("");
//            file.setFileSize("0");
//            file.setType(1);
//            straHisFileMapper.insertSelective(file);
//
//            MarketingStrategyProduct product1 = new MarketingStrategyProduct();
//            product1.setFileId(file.getId());
//            product1.setApiCode(apiCode);
//            product1.setCusBatchNumber(number);
//            product1.setBatchNumber(number);
//            product1.setStrategyId("DTM_BR0000005");
//            product1.setProductName("scorencashonszyxxy");
//            product1.setProductVersion("S1_0");
//            product1.setIsDel(1);
//            product1.setCreateTime(date1);
//
//            MarketingStrategyProduct product2 = new MarketingStrategyProduct();
//            product2.setFileId(file.getId());
//            product2.setApiCode(apiCode);
//            product2.setCusBatchNumber(number);
//            product2.setBatchNumber(number);
//            product2.setStrategyId("DTM_BR0000005");
//            product2.setProductName("scoremcashonxhqbdzcd");
//            product2.setProductVersion("S1_0");
//            product2.setIsDel(1);
//            product2.setCreateTime(date1);
//            productMapper.insertSelective(product1);
//            productMapper.insertSelective(product2);
//
//            MarketingTask task = new MarketingTask();
//            task.setApiCode(apiCode);
//            task.setBatchNumber(number);
//            task.setFileName("1");
//            task.setStrategyId("DTM_BR0000005");
//            task.setFrequency("1");
//            task.setCreateTime("2021-06-19 00:00:00");
//            task.setUpdateTime("2021-06-19 01:00:00");
//            task.setMonitorStatus(1);
//            task.setStatus(2);
//            task.setTaskNumber(0);
//            task.setActualNumber(0);
//            task.setIncrement(0);
//            task.setBegin(0);
//            task.setEnd(0);
//            task.setTableName("1");
//            task.setStrategyName("1");
//            task.setStartDate("2021-06-19");
//            task.setCloseDate("2021-06-20");
//            task.setMonitorModel(0);
//            task.setIsCheck(0);
//            task.setHitDate("");
//            task.setErrorMessage("");
//            task.setCusBatch(number);
//            task.setMonitorType(0);
//            task.setQueryBeginDate("2021-06-19");
//            task.setQueryEndDate("2021-06-20");
//            task.setStart(0);
//            task.setLimit(0);
//            task.setStrategyType("1");
//            task.setIsRepair("1");
//            task.setDataVolume(0);
//            marketingTaskMapper.insertTask(task);
//
//            String filePath = "D:\\data\\test\\"+number+".text";
//            File file1 = new File(filePath);
//            Path path = Paths.get(filePath);
//            if(!file1.getParentFile().exists()){
//                file1.getParentFile().mkdirs();
//            }
//            if(!file1.exists()){
//                try {
//                    file1.createNewFile();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            try(BufferedWriter writer =
//                        Files.newBufferedWriter(path, StandardCharsets.UTF_8,
//                                StandardOpenOption.APPEND)) {
//                writer.write("batchNumber,cusNum,scorencashonszyxxy_score,scoremcashonxhqbdzcd_score\r\n");
//                ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(50, 50, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<>(5000));
//                for (int i = 0; i < 1000000; i++) {
//                    threadPoolExecutor.submit(()->{
//                        //region 处理 数据入es和文件
//                        String cusNum = "yp_b_" + String.valueOf(m)+"s_"+ i;
//                        MarketingHistory bean = new MarketingHistory();
//                        bean.setApiCode(apiCode);
//                        bean.setIdCard("120222199" + String.valueOf(m) + i);
//                        bean.setCell("188" + String.valueOf(m) + i);
//                        bean.setName("燕萍_" + String.valueOf(m) + i);
//                        bean.setRequestTime(date);
//                        bean.setBatchNumber(number);
//                        bean.setCusBatchNumber(number);
//                        bean.setCusNum(cusNum);
//                        bean.setStrategyId("DTM_BR0000005");
//                        bean.setVersion("1");
//                        bean.setFileId(file.getId().toString());
//                        List<Product> products = new ArrayList<>();
//                        for (int j = 0; j < 2; j++) {
//                            Product product = new Product();
//                            if (j == 0) {
//                                product.setCode("scorencashonszyxxy");
//                                product.setVersion("S1_0");
//                                product.setCodeVersion("scorencashonszyxxy_S1_0");
//                                scoreA = new BigDecimal(scoreA + 0.001).setScale(2,BigDecimal.ROUND_DOWN).doubleValue();
//                                product.setScore(scoreA);
//                            } else {
//                                product.setCode("scoremcashonxhqbdzcd");
//                                product.setVersion("S1_0");
//                                product.setCodeVersion("scoremcashonxhqbdzcd_S1_0");
//                                scoreB = new BigDecimal(scoreB + 0.001).setScale(2,BigDecimal.ROUND_DOWN).doubleValue();
//                                product.setScore(scoreB);
//                            }
//                            product.setFlag("1");
//                            products.add(product);
//                        }
//                        bean.setProduct(products);
//                        marketingHistoryEsService.insert(bean, UUID.randomUUID().toString());
//                        writer.write(number.concat(",").concat(cusNum).concat(",").concat(String.valueOf(scoreA))
//                                .concat(",").concat(String.valueOf(scoreB)).concat("\r\n"));
//                        //endregion
//                    });
//
//                }
//            }catch(Exception ex){
//                System.out.println(ex.getMessage());
//            }
//            //endregion
//            }).start();
//        }
//
//        while(true){
//            try {
//                Thread.sleep(10000L);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }redis存1亿条数据占用内存

    }

    @Autowired
    MarketingUserMapper marketingUserMapper;

    @Test
    public void testGroup(){
        List<CustGroupTempVO> groupTypes = marketingUserMapper.selectGroupByCodeAndTime("7410437", "2021-07-13");
        System.out.println(groupTypes.toString());
    }

    @Test
    public void testGrovvy(){

        GroovyClassLoader classLoader = new GroovyClassLoader();
        Class groovyClass = classLoader.parseClass("def cal(object o){\n" +
                "    if(o.userType=='1'){return true} \n" +
                "       return false\n" +
                "}");
        try {
//            Object[] param = { 8,7 };
            MarketingSyncUser user = new MarketingSyncUser();
            user.setGroupType("1");
            GroovyObject groovyObject =
                    (GroovyObject) groovyClass.newInstance();
            boolean result = (boolean)groovyObject.invokeMethod("cal",user);
            System.out.println(result);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    public void testBF(){
//            Config config = new Config();
//            config.useSingleServer().setAddress("redis://127.0.0.1:6379");
//            config.useSingleServer().setPassword("1234");
//            //构造Redisson
//            RedissonClient redisson = Redisson.create(config);
//
//            RBloomFilter<String> bloomFilter = redisson.getBloomFilter("phoneList");
//            //初始化布隆过滤器：预计元素为100000000L,偏差率为3%
//            bloomFilter.tryInit(100000000L,0.03);
//            //将号码10086插入到布隆过滤器中
//            bloomFilter.add("10086");
//
//            //判断下面号码是否在布隆过滤器中
//            //输出false
//            System.out.println(bloomFilter.contains("123456"));
//            //输出true
//            System.out.println(bloomFilter.contains("10086"));
    }
    @Resource
    DecodeGrpcClient decodeClient;


    @Test
    public void testCellMd5() {

        String name = RpcClientProxy.decode("1622dc9b6b57a5faf337b87b13fc1200","cell" ,"md5","");
        System.out.println(name);

    }
/*@Resource
    DecodeClient decodeClient;


    @Test
    public void test() {

        String name = decodeClient.decode("id", "c74adbb43d009d0d6e96bbaeeb1c8bac", "1003", "", "1231231231");
        System.out.println(name);

    }
 @Test
    public  void testLog(){
        long l = System.currentTimeMillis();
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i=0;i<400;i++){
                    log.error("ceshi注入 StringRedisTemplate, 使用默认配置");
                }
            }
        }).start();

        log.error("{}",System.currentTimeMillis()-l);
    }*/

/*    @Test
    @SuppressWarnings("all")
    public void testExecutePipelined() {
      //  JedisCluster jedisCluster = JedisClusterUtil.getInstance().getJedisCluster();

        Set<HostAndPort> nodes = new HashSet<HostAndPort>();
        nodes.add(new HostAndPort("redis-cluster1-01.brapp.com", 7360));
        nodes.add(new HostAndPort("redis-cluster1-02.brapp.com", 7360));
        nodes.add(new HostAndPort("redis-cluster1-03.brapp.com", 7360));

        JedisCluster jc = new JedisCluster(nodes);

        JedisClusterPipeline pipelined = jcp.pipelined(jc);
        long s = System.currentTimeMillis();
       // jcp.refreshCluster();
        List<Object> batchResult = null;
        try {
            // batch write

 for (int i = 0; i < 10000; i++) {
                jcp.set("k" + i, "v1" + i);
            }
            jcp.sync();
            String[] ss={"cnt_loan_test:4002055:TotalLoan:totalCount", "cnt_loan_test:4002055:KeyAttribution:totalCount", "cnt_loan_test:4002055:Consumption_c:totalCount", "cnt_loan_test:4002055:ApplyLoanMon:totalCount", "cnt_loan_test:4002055:ApplyLoan_d:totalCount", "cnt_loan_test:4002055:Media_c:totalCount", "cnt_loan_test:4002055:Stability_c:totalCount", "cnt_loan_test:4002055:ApplyLoanStr:totalCount", "cnt_loan_test:4002055:SpecialList_c:totalCount", "cnt_loan_test:4002055:InfoRelation:totalCount"};

            // batch read
            for (int i=0;i<ss.length;i++) {
                jcp.get(ss[i]);
            }
            batchResult = jcp.syncAndReturnAll();
        } finally {
            jcp.close();
        }

        // output time
        long t = System.currentTimeMillis() - s;
        System.out.println(t);

        System.out.println(batchResult.size());
        // 实际业务代码中，close要在finally中调，这里之所以没这么写，是因为懒
        try {
            jc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 实际业务代码中，close要在finally中调，这里之所以没这么写，是因为懒
   // 使用 RedisCallback 把命令放在 pipeline 中
        RedisCallback<Object> redisCallback = new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                String[] s={"cnt_loan_test:4002055:TotalLoan:totalCount", "cnt_loan_test:4002055:KeyAttribution:totalCount", "cnt_loan_test:4002055:Consumption_c:totalCount", "cnt_loan_test:4002055:ApplyLoanMon:totalCount", "cnt_loan_test:4002055:ApplyLoan_d:totalCount", "cnt_loan_test:4002055:Media_c:totalCount", "cnt_loan_test:4002055:Stability_c:totalCount", "cnt_loan_test:4002055:ApplyLoanStr:totalCount", "cnt_loan_test:4002055:SpecialList_c:totalCount", "cnt_loan_test:4002055:InfoRelation:totalCount"};

                for (int i=0;i<s.length;i++) {
                    connection.get(s[i].getBytes());
                }
                return null;
            }
        };
        System.out.println(redisTemplate.executePipelined(redisCallback));

        // 使用 SessionCallback 把命令放在 pipeline
 SessionCallback<Object> sessionCallback = new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {

                operations.opsForValue().set("name", "qinyi");
                operations.opsForValue().set("gender", "male");
                operations.opsForValue().set("age", "19");

                return null;
            }
        };

        //System.out.println(stringRedisTemplate.executePipelined(redisCallback));
        System.out.println(redisTemplate.executePipelined(sessionCallback));
    }*/

    @Autowired
    SyncLogMapper syncLogMapper;

    @Test
    public void testmapsql(){
        Map<String,String> params=new HashMap<>();
        params.put("apiCode","7410784");
        params.put("fileName","abc");
//        params.put("createFileTime",createFileTime);
//        params.put("srcPath",syncConfig.getSrcSftpHost().concat(":").concat(syncConfig.getSrcPath()));
        List<SyncLog> syncLogs = syncLogMapper.querySyncLog(params);

        params.put("createFileTime","2021-11-09");
        syncLogMapper.querySyncLog(params);
        List<SyncLog> syncLogs2 = syncLogMapper.querySyncLog(params);
    }




    @Autowired
    ProductFlagScoreMapper productFlagScoreMapper;

    @Test
    public void testExample(){
        for (int i = 0; i < 50; i++) {
            new Thread(()->{
                ProductFlagScoreExample flagScoreExample = new ProductFlagScoreExample();
                flagScoreExample.createCriteria().andIsDelEqualTo(1);
                try {
                    List<ProductFlagScore> productFlagScores = productFlagScoreMapper.selectByExample(flagScoreExample);
                }catch (Exception ex){
                    System.out.println(ex.getMessage());
                }
            }).start();
        }
    }

    @Test
    public void addvalueRedis(){
        String key = "marketing:transfer:pushcustomer:apicode";
        ArrayList<String> objects = new ArrayList<>();
        objects.add("7410787");
        objects.add("3710004");
        objects.add("4004643");
        objects.add("3710012");
        redisChgService.sadd(key,objects);
    }

    @Test
    public void decodeTestByA(){
        String str = "UVZΒ5T";
        List<String> strings = Splitter.on(",").splitToList(str);
        System.out.println("解密开始");
        strings.forEach(t->{
            System.out.println(t.concat(",").concat(com.br.common.util.BrCipherMaker.getInstance().decode(t)));
        });
    }



    @Test
    public void desc(){
        JSONArray objects = JSONArray.parseArray("[{\"id\":283074,\"phone\":\"xowlWS+FJYNIFzcMUvDmsA==\"},{\"id\":199439,\"phone\":\"IdXR+LHGRkpnKbVawyvtMw==\"},{\"id\":256011,\"phone\":\"tL5AvKsiC9QUzqEfaEiWEQ==\"},{\"id\":283075,\"phone\":\"lMUkp6FvbBHAVMUbo3uxpg==\"},{\"id\":199440,\"phone\":\"ngol0y13+8Sop84Uv+KcDQ==\"},{\"id\":256012,\"phone\":\"DEIhAMDuY/uG/Q4Tag2RFA==\"},{\"id\":199441,\"phone\":\"oCwy0JFrWki1C5btIDhowg==\"},{\"id\":283076,\"phone\":\"Gey/IVCfDyXoWjpyx5YwDA==\"},{\"id\":256013,\"phone\":\"1s3ekXYtMGyKBCNvCRXetQ==\"},{\"id\":36543,\"phone\":\"sI56roRJVF6DYyih6Dr4qA==\"},{\"id\":283077,\"phone\":\"KxsqRFJPXN/8GfCEp4vEwg==\"},{\"id\":256014,\"phone\":\"MBuTqNEVrz+0hsJJJNEPWA==\"},{\"id\":36544,\"phone\":\"tNfAZYIKRyBXAbg3nSCkIQ==\"},{\"id\":283078,\"phone\":\"Hbk22zyrPNDp6g9N9KWnxw==\"},{\"id\":256015,\"phone\":\"Em0fAej0ZsklPA4wDnavlQ==\"},{\"id\":256017,\"phone\":\"y8uE3syM9wtBlIVD8tDg2g==\"},{\"id\":256018,\"phone\":\"FtlC8icAjbh+vt4KHnIu3Q==\"},{\"id\":300001,\"phone\":\"D5ghHzahjBzsIHYYDu6eHw==\"},{\"id\":156356,\"phone\":\"AOPbG8ZvA6q6WM+n9q7Rsw==\"},{\"id\":256019,\"phone\":\"Eh+3K6EzvULDuAf2ihX2OQ==\"},{\"id\":156357,\"phone\":\"YIikNVeZmMkCJf4+sYWo+A==\"},{\"id\":36545,\"phone\":\"ogAF8SXzpfHydwT7+vT8ug==\"},{\"id\":256020,\"phone\":\"p4qsUFPf8OVFVLU3gI1YCA==\"},{\"id\":224720,\"phone\":\"+Mclya2LMz/I57KexSuwhA==\"},{\"id\":36546,\"phone\":\"LrxY4yz/axNXsT7Xs/zzKw==\"},{\"id\":224721,\"phone\":\"sGn24Bna/mLoRoM+O0Zs0Q==\"},{\"id\":283079,\"phone\":\"HpXGhMmZixBZKNdv95is1A==\"},{\"id\":256021,\"phone\":\"6WrXaK8J+koheC0MltfIkQ==\"},{\"id\":36547,\"phone\":\"pu3fURdmSUR0hwkObTCesg==\"},{\"id\":19534,\"phone\":\"gGyv0sJDYiO0/zwnnm+bYQ==\"},{\"id\":300002,\"phone\":\"WQ9jU5sbWzgxJ095yjj2eA==\"},{\"id\":36548,\"phone\":\"sUc5VJZNDFGQ7nPGdwLetA==\"},{\"id\":36549,\"phone\":\"hUYkna2v54WWpQ+XnVRtUg==\"},{\"id\":199442,\"phone\":\"YiQzwO9PmPF9LormB+0yvw==\"},{\"id\":283080,\"phone\":\"2ZG6x40Ak4tUy3+Q5t1oiQ==\"},{\"id\":283081,\"phone\":\"W7qF0Wj57huOfV+sASH5yw==\"},{\"id\":199443,\"phone\":\"ZAGe9YD9dSDDtZVmUUJsXw==\"},{\"id\":224722,\"phone\":\"tx2Rz4vMvREMA6IXJ0UA5Q==\"},{\"id\":256022,\"phone\":\"KuXoOeB+oYRJpJqCYfjthg==\"},{\"id\":283082,\"phone\":\"dZ9pO7BgB+BRpNOCdb7iMg==\"},{\"id\":256023,\"phone\":\"dj4SIexITEY4SALQyoTHiA==\"},{\"id\":19535,\"phone\":\"HYJA1jXZedYNGRj5vrj/wQ==\"},{\"id\":36550,\"phone\":\"1CxEqGgH6ziATSQhAH8N5g==\"},{\"id\":36551,\"phone\":\"UPWy95RGa/q7hDaRgyV3lQ==\"},{\"id\":36552,\"phone\":\"8U6dTuHvbZsk3TM4AwcX4A==\"},{\"id\":36553,\"phone\":\"PRsc79uz1W0ni3VWEqcMIQ==\"},{\"id\":36554,\"phone\":\"QWCzkGo8vrC8AfJ1O5xvRA==\"},{\"id\":36555,\"phone\":\"bP2GZoxZH5qPN9chYD+Olg==\"},{\"id\":36556,\"phone\":\"/fInKRqniblEg2+6Lif7gQ==\"},{\"id\":156358,\"phone\":\"Y60/mjo5bHLTIl14edB64g==\"},{\"id\":36557,\"phone\":\"YOkgluyi+9fs3MWDKc48ww==\"},{\"id\":300003,\"phone\":\"XMiQK8N3Aj74Q04eVbvo4w==\"},{\"id\":36558,\"phone\":\"NJMMl5rrowu8HhCDQNMkcg==\"},{\"id\":256027,\"phone\":\"TjUEisB+h8YYLUfV+PeK5w==\"},{\"id\":36559,\"phone\":\"P8gTLC0iUcqe2H9dtanc2A==\"},{\"id\":156359,\"phone\":\"aZetBxFAlMVEFciy5BE+dA==\"},{\"id\":156360,\"phone\":\"rxUcQRND8uY1k9F/C7z3jA==\"},{\"id\":36560,\"phone\":\"U/W25KX2JqSSs6+qpGU91A==\"},{\"id\":36561,\"phone\":\"O9WrEIv1NdRUl3PQdlztzQ==\"},{\"id\":36562,\"phone\":\"f24veOu/poNLUMjKZoL9yg==\"},{\"id\":199444,\"phone\":\"ClH07KB+cmRakr7fSSObGg==\"},{\"id\":36563,\"phone\":\"cMeLgoqpgPZDMMQxdNBzjw==\"},{\"id\":36564,\"phone\":\"FS162fSu7b9wugZGkDccDg==\"},{\"id\":256029,\"phone\":\"yI97rfb/vNsKvHpUXEjb9A==\"},{\"id\":36565,\"phone\":\"yNpUR294rYOiKE3+Q3jVFw==\"},{\"id\":36566,\"phone\":\"FFy+fZg1JbE/JcctwfjK/w==\"},{\"id\":156361,\"phone\":\"ojUqasK5rZrpGi1VLJRvOA==\"},{\"id\":36567,\"phone\":\"+33ef0GBER+NGD6DJRNhqQ==\"},{\"id\":36568,\"phone\":\"s/zyIuML6G4xFsFIVUmO8Q==\"},{\"id\":36569,\"phone\":\"NaAZ5BolBNGpPWKCAF2e5A==\"},{\"id\":36570,\"phone\":\"mtPsZmFMglzEDFZjnu7W3A==\"},{\"id\":199445,\"phone\":\"CWTKPBgW52C2vstA04VgPw==\"},{\"id\":224723,\"phone\":\"S1uDVIcDxsqPR0LpWTcrkA==\"},{\"id\":156362,\"phone\":\"QjgTGqMp8f3Qa4+9p2sDUA==\"},{\"id\":36572,\"phone\":\"OKdkOzwtwC8QF7TnDEF55A==\"},{\"id\":36573,\"phone\":\"nJodJXJKfvbH3wV1hl7KzA==\"},{\"id\":156363,\"phone\":\"xpQmbKJXyPVXJm3Io5HhMg==\"},{\"id\":36574,\"phone\":\"JJPof9496DuRetutn6xXDw==\"},{\"id\":36575,\"phone\":\"C1tzEbdhe83FIvWg8WrIZg==\"},{\"id\":19536,\"phone\":\"X/TDRNtrs5VhLLUAAVjx/Q==\"},{\"id\":156364,\"phone\":\"uc/F5/AWRrtwT4N2fiK8yw==\"},{\"id\":36576,\"phone\":\"+sny5HsXuhspOKYuyXo/ig==\"},{\"id\":36577,\"phone\":\"F/DUvx3yG7T5xtdg0wFqQA==\"},{\"id\":224724,\"phone\":\"M9KwyNdpPigWOks63KJiNw==\"},{\"id\":36578,\"phone\":\"S3Eb8FiDDtkPRFAwmpyzJA==\"},{\"id\":36579,\"phone\":\"fybj4cSEW/NG/OII8kofpw==\"},{\"id\":36580,\"phone\":\"hkNmqDuuQN8So+mLWZDfdg==\"},{\"id\":36581,\"phone\":\"2n/DZNXyTtCQ5Z/MdGysUg==\"},{\"id\":36582,\"phone\":\"Asmi/cmmsTkQ+XUu+SGlOg==\"},{\"id\":224725,\"phone\":\"PRYR4aYX1aJq4+WOssQ9XQ==\"},{\"id\":36583,\"phone\":\"cSzNzxhDXtkDDUuEtQdf/Q==\"},{\"id\":36584,\"phone\":\"FXZgHOl0re4re/VvQ6fdCA==\"},{\"id\":19538,\"phone\":\"5CYmlO2lf7jbWO370r8q0A==\"},{\"id\":224726,\"phone\":\"jSIamZw6l/lSg5NgimmSBw==\"},{\"id\":300004,\"phone\":\"ufPk8LSsXZmDxID8S6zFHQ==\"},{\"id\":36585,\"phone\":\"gHoC0itVhP8EU/T/xyuxtA==\"},{\"id\":156365,\"phone\":\"7pUbJJeNsmxPVlLIl6peRg==\"},{\"id\":156366,\"phone\":\"oRqUFtJzzQ0sHciOTzFGug==\"},{\"id\":224727,\"phone\":\"5HI9lOi33LIMLZMaMsV4fw==\"},{\"id\":199446,\"phone\":\"v/wsC5dWWKLNWUCXRMhztw==\"},{\"id\":300005,\"phone\":\"0s22PJXCGTitnETaO9N+1w==\"},{\"id\":156367,\"phone\":\"yoHPjQebfhqDgFORee9xxA==\"},{\"id\":199448,\"phone\":\"wph+KSMOoOVZwb+qMhtN4w==\"},{\"id\":256033,\"phone\":\"hdczWrINkVD7WXGsrR014g==\"},{\"id\":224728,\"phone\":\"t1Ynpl/ZNKq+C3Sz8rojqQ==\"},{\"id\":256034,\"phone\":\"lyNdJ0wWP9gpkJ4wDAimEA==\"},{\"id\":36586,\"phone\":\"xS8uDI1gWYwUMQdC5hJhwQ==\"},{\"id\":199449,\"phone\":\"6sxiC7CNXizEAkpLevQpGQ==\"},{\"id\":36587,\"phone\":\"dHFWnjAW70KswNCD9doshw==\"},{\"id\":36588,\"phone\":\"83PgMW5ByCQDHXbHNYp+HA==\"},{\"id\":199450,\"phone\":\"2Eh6dZDnHY6eqlruqKzMKQ==\"},{\"id\":300006,\"phone\":\"DLWdyCorGmEIFHhTCvHThg==\"},{\"id\":36589,\"phone\":\"JGHcn2A9RjwKsPGdiTCAlA==\"},{\"id\":36590,\"phone\":\"pzp7FDHU5Mt6DxvZaOrO2w==\"},{\"id\":224729,\"phone\":\"QH/cYiaoBvFSNipcAbeH8w==\"},{\"id\":156368,\"phone\":\"JGrxD66EaABjqTdxQbhmpw==\"},{\"id\":36591,\"phone\":\"UUnwcoEfFcVWJAUddAg2fw==\"},{\"id\":156369,\"phone\":\"OZSnum8Eem71yJmdLhBiZQ==\"},{\"id\":300007,\"phone\":\"4LaRsFNOmit4pQ1uDmmaSg==\"},{\"id\":36592,\"phone\":\"gDqHQo2zRM2AmnPDHze5WA==\"},{\"id\":36593,\"phone\":\"Tx7t2A61Ln8nukgMFJoBfA==\"},{\"id\":156370,\"phone\":\"ojsYV18UZLmIW2kTX5pzRA==\"},{\"id\":36594,\"phone\":\"q7Q6QagSINRwgEJg2rruzQ==\"},{\"id\":156371,\"phone\":\"nfbd28KGbgtptM6ZUYpxBQ==\"},{\"id\":156372,\"phone\":\"JY+w1VsqIed0sPMnEvNjSA==\"},{\"id\":156373,\"phone\":\"2zoAggIirsdnptDjuTCuuQ==\"},{\"id\":36595,\"phone\":\"lxxDsHecE0oUInLsnRW0XA==\"},{\"id\":300008,\"phone\":\"njhrnPW//Dz5Ujtg2DCZ2w==\"},{\"id\":36596,\"phone\":\"QP4a1k7bi3UWfuqvxjaQuQ==\"},{\"id\":300009,\"phone\":\"4azluWFMp/R+c7xD9Z1jXQ==\"},{\"id\":199454,\"phone\":\"C5mUBLx7ToEIcmi1MaqKgA==\"},{\"id\":19539,\"phone\":\"4D52UfoVCiI5zvKCTx3gUg==\"},{\"id\":36597,\"phone\":\"fci8VRW3Zu2diMKYBFIdGg==\"},{\"id\":300010,\"phone\":\"kr4XHuOheDJvztiivctt/g==\"}]");
        StringBuilder stringBuilder = new StringBuilder();
        String updateSql_temp = "update b_phone_sale set phone = '%s',phone_aes='%s' where id = %d;";
        objects.forEach(t->{
             JSONObject t1 = (JSONObject) t;
             Long id = t1.getLong("id");
             String phone_old = t1.getString("phone");
             String phone_new = AESUtil.decrypt(phone_old, "ovksl39fcl13m9dF");
             String phone_new_aes = AESUtil.aesEncrypty(phone_new, "MgoTm8GxuxTUc6y5");
             String phone_log = com.br.common.util.BrCipherMaker.getInstance().encode(phone_new);
             String updateSql = String.format(updateSql_temp, phone_new_aes, phone_log, id);
            stringBuilder.append(updateSql);
        });
        System.out.println(stringBuilder);

    }

    @Test
    public void testHisFile() {
        StraHisFileExample hisFileExample = new StraHisFileExample();
        try {
            hisFileExample.createCriteria()
                    .andApiCodeEqualTo("7410437")
                    .andBatchNumberEqualTo("7410437_20220412181100_3806")
                    .andCreateTimeGreaterThanOrEqualTo(new SimpleDateFormat("yyyy-MM-dd").parse("2022-04-18"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        List<StraHisFile> straHisFiles = straHisFileMapper.selectByExample(hisFileExample);
        StraHisFile file = straHisFiles.get(0);
        System.out.println(file);
    }

    @Test
    public void phoneAES(){
        String content = "";
        JSONArray objects = JSONArray.parseArray(content);
        objects.forEach(t->{
            JSONObject t1 = (JSONObject) t;
            String localId = t1.getString("localId");
            String custNum = t1.getString("custNum");
            String phone = t1.getString("phone");
            String phoneReal = com.br.common.util.BrCipherMaker.getInstance().decode(phone);
            String aesPhone = AESUtil.aesEncrypty(phoneReal, "MgoTm8GxuxTUc6y5");
            System.out.println(String.format("update b_phone_sale set phone_aes='%s',phone='%s' where local_id=%s and uid = '%s';"
                    ,aesPhone,phone,localId,custNum));
            System.out.println(String.format("update b_phone_black set phone='%s' where local_id=%s;"
                    ,phone,localId));
        });
    }

    public void xcSign(){
        String appid = "bairong001";
        String aesKey="f3df6f62f0527bf0";
        String aesIv="3b2dac323465b024";
        String signKey="95cc01ec07387a44";
    }

    @Test
    public void testError(){
        try (
                FileReader read = new FileReader("/opt/data/660014.txt");
                BufferedReader br = new BufferedReader(read);) {
            StringBuilder abc = new StringBuilder();
            String row;
            Integer line = 1;
            ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(20, 20);
            while ((row = br.readLine()) != null) {
                abc.append(row);
            }
            String[] split = abc.toString().split("\\|\\|");
            try (Writer fw = new BufferedWriter(
                    new OutputStreamWriter(
                            Files.newOutputStream(Paths.get("/opt/data/660014_error.txt")), StandardCharsets.UTF_8));) {


            for (String s : split) {
                JSONObject jsonObject = JSON.parseObject(s);
                for (String s1 : jsonObject.keySet()) {
                    JSONArray jsonArray = jsonObject.getJSONArray(s1);
                    String cell = JSON.parseObject(jsonArray.getString(0)).getString("手机号");
                    fw.append(s1+","+cell+"\r\n");
                }
            }
            } catch (Exception e) {

            }
        }catch (Exception ex){

        }


    }


    @Test
    public void vaildPPD(){
        try (
                FileReader read = new FileReader("/opt/data/3710014_R20220721001_3710014_20220722000000_3177__20220722_20220722.txt");
                BufferedReader br = new BufferedReader(read);
                FileReader custRead = new FileReader("/opt/data/custnum.txt");
                BufferedReader custBr = new BufferedReader(custRead);
                Writer fw = new BufferedWriter(
                        new OutputStreamWriter(
                                Files.newOutputStream(Paths.get("/opt/data/custnum_should_ppddass.txt")), StandardCharsets.UTF_8));) {
            String custStr;
            HashSet<String> strings = new HashSet<>();
            while ((custStr = custBr.readLine()) != null) {
                strings.add(custStr);
            }

            StringBuilder abc = new StringBuilder();
            String row;
            Integer line = 1;
            Integer pushnum = 0;
            ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(20, 20);
            while ((row = br.readLine()) != null) {
                if(line > 1){
                    String[] split = row.split(",",-1);
                    if(strings.contains(split[2])&&StringUtils.isNotBlank(split[13])&&Long.valueOf(split[13])>=60){
                        fw.append(split[2]).append(",").append(split[13]).append("\r\n");
                        pushnum++;
                    }
                }
                line++;

            }
        }catch (Exception ex){

        }
    }
}
