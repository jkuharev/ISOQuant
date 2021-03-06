<map version="0.9.0">
<!-- To view this file, download free mind mapping software FreeMind from http://freemind.sourceforge.net -->
<node CREATED="1300120526452" ID="ID_762130467" MODIFIED="1320825924896" TEXT="ISOQuant 2">
<node CREATED="1301045400172" ID="ID_1007032202" MODIFIED="1301045412373" POSITION="right" TEXT="DB">
<node CREATED="1301045412374" ID="ID_1112161860" MODIFIED="1301045438162" TEXT="2D project design">
<node CREATED="1301045455891" ID="ID_392206157" MODIFIED="1301045467640" TEXT="vertical group"/>
<node CREATED="1301045468355" ID="ID_219876262" MODIFIED="1301045475577" TEXT="horizontal group"/>
<node CREATED="1301045477107" ID="ID_1517745923" MODIFIED="1301045493705" TEXT="sample related to multiple groups"/>
</node>
<node CREATED="1301045500451" ID="ID_796136469" MODIFIED="1301045910689" TEXT="raw file related">
<node CREATED="1301045577235" ID="ID_215889483" MODIFIED="1301045856153" TEXT="project -(1:n)&gt; groups -(m:n)&gt; samples -(1:n)&gt; raw-files -(1:n)&gt; processing results"/>
<node CREATED="1301045912331" ID="ID_536122356" MODIFIED="1301045936417" TEXT="raw-file types">
<node CREATED="1301045930829" ID="ID_1338670452" MODIFIED="1301045948176" TEXT="Quantification run"/>
<node CREATED="1301045948619" ID="ID_1487575948" MODIFIED="1301045952168" TEXT="DDA run">
<node CREATED="1301045957909" ID="ID_1661479422" MODIFIED="1301046014792" TEXT="for EMRT-Cluster annotation only"/>
<node CREATED="1301046167691" ID="ID_1061503564" MODIFIED="1301046193712" TEXT="RTW possible"/>
</node>
</node>
</node>
</node>
<node CREATED="1305787088158" ID="ID_664116453" MODIFIED="1305787100281" POSITION="left" TEXT="Job oriented workflow">
<node CREATED="1305787101474" ID="ID_1076688073" MODIFIED="1305787114849" TEXT="JobSet">
<node CREATED="1305787115754" ID="ID_207929322" MODIFIED="1305787209287" TEXT="Job[]">
<node CREATED="1305787129010" ID="ID_1709513295" MODIFIED="1305787191280" TEXT="project[]"/>
<node CREATED="1305787143842" ID="ID_522655829" MODIFIED="1305787147936" TEXT="settings"/>
<node CREATED="1305787152466" ID="ID_1061702025" MODIFIED="1305787188400" TEXT="io/db/... handlers"/>
</node>
</node>
</node>
<node CREATED="1305787224666" ID="ID_1767106460" MODIFIED="1305787234880" POSITION="left" TEXT="settings management"/>
<node CREATED="1305876039547" ID="ID_1529038429" MODIFIED="1305876073723" POSITION="right" TEXT="support for prefractioned samples">
<node CREATED="1305876075466" ID="ID_1665563406" MODIFIED="1305876078833" TEXT="offline">
<node CREATED="1305876096450" ID="ID_533578029" MODIFIED="1305876099481" TEXT="protein"/>
<node CREATED="1305876099810" ID="ID_517805339" MODIFIED="1305876101753" TEXT="peptide"/>
</node>
<node CREATED="1305876079130" ID="ID_1382638978" MODIFIED="1305876080656" TEXT="online"/>
</node>
<node CREATED="1320825928499" ID="ID_1004358220" MODIFIED="1320825935113" POSITION="left" TEXT="RT Alignment">
<node CREATED="1320825936322" ID="ID_1744240414" MODIFIED="1320825976142" TEXT="in-sample"/>
<node CREATED="1320825976578" ID="ID_1992051367" MODIFIED="1320825984480" TEXT="cross-sample"/>
<node CREATED="1320825996178" ID="ID_118149127" MODIFIED="1320826017561" TEXT="cor-based (Timuba) alignment"/>
<node CREATED="1315555222547" ID="ID_66252734" MODIFIED="1315555228473" TEXT="Sequential Alignment">
<node CREATED="1315555458921" ID="ID_1780236767" MODIFIED="1315555466096" TEXT="miltiple alignment"/>
<node CREATED="1315555467457" ID="ID_1990322684" MODIFIED="1315555475912" TEXT="all vs all"/>
</node>
<node CREATED="1364373569282" ID="ID_1486039783" MODIFIED="1364373583901" TEXT="runs, die nichts miteinander zu tun haben!!!">
<node CREATED="1364373585915" ID="ID_65988341" MODIFIED="1364373646248" TEXT="evtl. paarweise runs,&#xa;die am Besten miteinander korrelieren&#xa;d.h. gleiche Proteine beinhalten?"/>
<node CREATED="1364373758064" ID="ID_264803246" MODIFIED="1364373815459" TEXT="binary hierarchical pairwise alignment&#xa;correlation matrix nearest neighbor"/>
</node>
</node>
<node CREATED="1334645763352" ID="ID_1040795431" MODIFIED="1334645769278" POSITION="right" TEXT="REPORT">
<node CREATED="1334645769278" ID="ID_1560841413" MODIFIED="1334645771308" TEXT="HTML"/>
<node CREATED="1334645772356" ID="ID_111590465" MODIFIED="1334645774122" TEXT="CSV"/>
<node CREATED="1334645774580" ID="ID_141705405" MODIFIED="1334645813077" TEXT="peptide nach start_pos innerhalb eines Proteins anordnen"/>
<node CREATED="1345647321602" ID="ID_714621118" MODIFIED="1345647324177" TEXT="XLS">
<node CREATED="1345647202179" ID="ID_1686989781" MODIFIED="1345647317740" TEXT="Correlation-Table">
<node CREATED="1345647333314" ID="ID_1115609765" MODIFIED="1345647350330" TEXT="based on Log(Intensity)"/>
</node>
</node>
</node>
<node CREATED="1363016349660" ID="ID_1753652543" MODIFIED="1363016362679" POSITION="left" TEXT="Protein Sequence Search">
<node CREATED="1363016364348" ID="ID_654149366" MODIFIED="1363016379190" TEXT="protein look up table"/>
<node CREATED="1363016406832" ID="ID_1854155204" MODIFIED="1363016424939" TEXT="do not rely on PLGS peptide-&gt;protein assignment"/>
</node>
</node>
</map>
