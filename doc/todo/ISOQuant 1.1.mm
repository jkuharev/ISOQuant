<map version="0.9.0">
<!-- To view this file, download free mind mapping software FreeMind from http://freemind.sourceforge.net -->
<node CREATED="1300120526452" ID="ID_762130467" MODIFIED="1301498456943" TEXT="ISOQuant 1.1">
<node CREATED="1300123277962" ID="ID_1277129471" MODIFIED="1300123621703" POSITION="left" TEXT="Develop">
<node CREATED="1300868133362" HGAP="22" ID="ID_60765444" MODIFIED="1318258233847" TEXT="GUI" VSHIFT="-30">
<node CREATED="1300120532826" ID="ID_1331655404" MODIFIED="1300884737633" TEXT="Exit Button">
<icon BUILTIN="button_ok"/>
</node>
<node CREATED="1300120560554" ID="ID_1531395377" MODIFIED="1300889219510" TEXT="place Popup-Windows centered over main Window">
<icon BUILTIN="button_ok"/>
</node>
<node CREATED="1300176389981" ID="ID_467175036" MODIFIED="1300956592150" TEXT="First Run Assistant">
<icon BUILTIN="full-5"/>
</node>
</node>
<node CREATED="1300372187619" ID="ID_795382836" MODIFIED="1300886488239" TEXT="step up to root folder from its inside (e.g. if project folder is selected)">
<icon BUILTIN="button_ok"/>
</node>
<node CREATED="1300179611448" ID="ID_1812536894" MODIFIED="1300956523800" TEXT="check expired connection and reconnect">
<icon BUILTIN="full-9"/>
</node>
<node CREATED="1300372087037" ID="ID_874085545" MODIFIED="1300956530606" TEXT="std output to log-file">
<icon BUILTIN="full-8"/>
</node>
<node CREATED="1300867946859" HGAP="22" ID="ID_1678655403" MODIFIED="1307449589510" TEXT="REPORT" VSHIFT="21">
<node CREATED="1300209810658" FOLDED="true" ID="ID_1350184069" MODIFIED="1306847943780" TEXT="HTML">
<node CREATED="1300209823467" ID="ID_1980554753" MODIFIED="1300956578862" TEXT="im&lt;title&gt;%PROJECT_TITLE%&lt;title&gt; wird nicht ersetzt">
<icon BUILTIN="button_ok"/>
</node>
<node CREATED="1301908779502" ID="ID_1968495605" MODIFIED="1301908785518" TEXT="Peptide">
<node CREATED="1301908785519" ID="ID_476693336" MODIFIED="1301908832519" TEXT="peptide.start - peptide.end"/>
</node>
</node>
<node CREATED="1300377134699" ID="ID_968991926" MODIFIED="1307449590926" TEXT="Excel">
<node CREATED="1300377163838" ID="ID_1445960051" MODIFIED="1300956542462" TEXT="sample mean, std">
<icon BUILTIN="full-3"/>
</node>
<node CREATED="1300123010343" ID="ID_103798135" MODIFIED="1300956555631" TEXT="output retention time alignment&#xa;PIVOT: select * from rtw">
<icon BUILTIN="full-3"/>
</node>
<node CREATED="1300377203414" ID="ID_1485441147" MODIFIED="1300956565254" TEXT="sample regulationsverh&#xe4;ltnisse">
<icon BUILTIN="full-9"/>
</node>
<node CREATED="1300377272918" ID="ID_1608913272" MODIFIED="1300956567430" TEXT="protein -&gt; sample-to-sample: t-test, log2-ratio">
<icon BUILTIN="full-9"/>
</node>
<node CREATED="1300958005387" ID="ID_417516905" MODIFIED="1300958061602" TEXT="COUNT(DISTINCTseq) FOR each peptide type"/>
<node CREATED="1300960130763" ID="ID_1511589477" MODIFIED="1300960166399" TEXT="SELECT `index`, `workflow_index`, `mass`, `sequence`, `type`, `modifier` &#xa;FROM `peptide`  &#xa;WHERE `type`!=&apos;MISSING_CLEAVAGE&apos; AND `sequence` RLIKE &apos;.*[RK][^P]+.*&apos; &#xa;GROUP BY `workflow_index`, `sequence`, `type`, `modifier` "/>
<node CREATED="1301391983092" ID="ID_1922555797" MODIFIED="1301392032959" TEXT="Count unique/razor/shared Peptides per Protein"/>
<node CREATED="1301473398602" FOLDED="true" ID="ID_702103445" MODIFIED="1307449597014" TEXT="pep based Quantification">
<node CREATED="1301473409818" ID="ID_1931270676" MODIFIED="1301473415237" TEXT="Protein"/>
<node CREATED="1301473415690" ID="ID_1842328364" MODIFIED="1301473451658" TEXT="number of unique+razor peptides"/>
<node CREATED="1301473452282" ID="ID_1712222408" MODIFIED="1301473483241" TEXT="median(Log2R(SampleA vs. SampleB ...))"/>
</node>
<node CREATED="1307449602475" ID="ID_784741606" MODIFIED="1307449618856" TEXT="RTW -&gt; Pivot-Table"/>
</node>
<node CREATED="1303981321580" ID="ID_1125695909" MODIFIED="1307618568304" TEXT="both">
<node CREATED="1303981326323" ID="ID_1688594671" MODIFIED="1303981496247" TEXT="Peptides per Protein having COUNT(RAZOR)=1 &amp; COUNT(DISTINCT peptides)=1&#xa;=&gt; Statistics: number of Peptides and Peptide Types for identification / quantification"/>
</node>
</node>
<node CREATED="1300956612691" HGAP="24" ID="ID_1907629714" MODIFIED="1307449650916" TEXT="Import" VSHIFT="26">
<node CREATED="1300956905035" ID="ID_911592842" MODIFIED="1300956912001" TEXT="Project Version">
<node CREATED="1300956737075" ID="ID_1826113571" MODIFIED="1300956930431" TEXT="PLGS 2.3">
<node CREATED="1306847890774" ID="ID_1798868812" MODIFIED="1306847925583" TEXT="MassSpectrum.xml">
<icon BUILTIN="button_ok"/>
</node>
</node>
<node CREATED="1300956661170" ID="ID_1743647702" MODIFIED="1300956753632" TEXT="PLGS 2.4">
<node CREATED="1306847890774" ID="ID_930385258" MODIFIED="1306847923329" TEXT="MassSpectrum.xml">
<icon BUILTIN="button_ok"/>
</node>
</node>
<node CREATED="1300956622867" ID="ID_107120208" MODIFIED="1304933046819" TEXT="PLGS 2.5">
<cloud/>
<font BOLD="true" NAME="SansSerif" SIZE="12"/>
<icon BUILTIN="full-1"/>
<node CREATED="1300956631226" ID="ID_1940031031" MODIFIED="1300956657785" TEXT="mit Ion-Mobility"/>
<node CREATED="1300956637234" ID="ID_1453893252" MODIFIED="1300956651145" TEXT="ohne Ion-Mobility"/>
<node CREATED="1306847890774" ID="ID_63030685" MODIFIED="1306847920480" TEXT="MassSpectrum.xml">
<icon BUILTIN="button_ok"/>
</node>
</node>
</node>
<node CREATED="1300956938803" HGAP="22" ID="ID_340165321" MODIFIED="1307449658349" TEXT="import type" VSHIFT="-16">
<node CREATED="1300956673923" ID="ID_626269774" MODIFIED="1307618548722" TEXT="by EA">
<icon BUILTIN="button_ok"/>
</node>
<node CREATED="1300956680618" ID="ID_1601852307" MODIFIED="1307618546077" TEXT="self designed">
<icon BUILTIN="button_ok"/>
<node CREATED="1305715649773" ID="ID_1529297391" MODIFIED="1307618563413" TEXT="create EMRT Table from MS">
<icon BUILTIN="button_ok"/>
</node>
</node>
</node>
</node>
<node CREATED="1301473262943" ID="ID_600502981" MODIFIED="1307449648246" TEXT="Peptide based Quantification">
<node CREATED="1301473284082" ID="ID_306314731" MODIFIED="1301473384194" TEXT="median(Log2R(Sample A vs Sample B ...))"/>
</node>
<node CREATED="1305807831882" ID="ID_750631536" MODIFIED="1307449642942" TEXT="MySQL">
<node CREATED="1305807838517" ID="ID_1128254053" MODIFIED="1306847735248" TEXT="executeFile">
<node CREATED="1305807849821" ID="ID_238993247" MODIFIED="1305807872361" TEXT="show file path"/>
<node CREATED="1305807872909" ID="ID_155950716" MODIFIED="1305807879669" TEXT="show user defined text"/>
<node CREATED="1305807880109" ID="ID_179592083" MODIFIED="1306847738169" TEXT="measure execution time">
<icon BUILTIN="button_ok"/>
</node>
</node>
<node CREATED="1305808788747" ID="ID_1710180243" MODIFIED="1305808867732" TEXT="&apos;connected to&apos; message">
<node CREATED="1305808841221" ID="ID_197095141" MODIFIED="1305808846891" TEXT="show default"/>
<node CREATED="1305808804618" ID="ID_441879191" MODIFIED="1305808826211" TEXT="suppress"/>
<node CREATED="1305808827565" ID="ID_82047873" MODIFIED="1305808856486" TEXT="show user defined"/>
</node>
</node>
<node CREATED="1306848004461" ID="ID_768950307" MODIFIED="1306848021625" TEXT="Expression Analysis">
<node CREATED="1306848007870" ID="ID_1399907210" MODIFIED="1306848096662" TEXT="Alignment">
<icon BUILTIN="button_ok"/>
<node CREATED="1306848026285" ID="ID_1973931095" MODIFIED="1306848089438" TEXT="Iterative">
<icon BUILTIN="button_ok"/>
<node CREATED="1306848064869" ID="ID_1628649677" MODIFIED="1306848082020" TEXT="Path Refining Retention Time Alignment"/>
</node>
<node CREATED="1306848031173" ID="ID_1983710059" MODIFIED="1306848130648" TEXT="Recursive">
<icon BUILTIN="button_ok"/>
<node CREATED="1306848036509" ID="ID_1021939279" MODIFIED="1306848050060" TEXT="Single Threaded Hirschberg"/>
<node CREATED="1306848050669" ID="ID_627210999" MODIFIED="1306848062102" TEXT="Parallelized Hirschberg"/>
</node>
</node>
<node CREATED="1306848098213" ID="ID_931135642" MODIFIED="1306848139247" TEXT="Clustering">
<icon BUILTIN="button_ok"/>
<node CREATED="1306848103157" ID="ID_1930309821" MODIFIED="1306848135930" TEXT="Hierarchical-Non-Hierarchical">
<icon BUILTIN="button_ok"/>
</node>
</node>
</node>
</node>
<node CREATED="1300123291250" HGAP="44" ID="ID_303105541" MODIFIED="1306847959734" POSITION="right" TEXT="Paper" VSHIFT="-68">
<node CREATED="1300123676866" ID="ID_1170250937" MODIFIED="1300868119152" TEXT="Bilder">
<node CREATED="1300123380170" ID="ID_1418217775" MODIFIED="1300123699021" TEXT="Distrubution Loop"/>
<node CREATED="1300123700618" ID="ID_1014558401" MODIFIED="1300123708647" TEXT="Retention Time Alignment"/>
<node CREATED="1300123709042" ID="ID_1811267757" MODIFIED="1300123714727" TEXT="Clustering"/>
</node>
</node>
<node CREATED="1301498457999" HGAP="19" ID="ID_527101862" MODIFIED="1306848261244" POSITION="right" TEXT="to fix" VSHIFT="25">
<node CREATED="1301649373532" ID="ID_1462898474" MODIFIED="1305715680155" TEXT="show expression analysis &#xa;for projects in DB">
<node CREATED="1301649418619" ID="ID_1678855409" MODIFIED="1301649506837" TEXT="in About Project"/>
<node CREATED="1301649435627" ID="ID_1973895238" MODIFIED="1301649477430" TEXT="in Main Windows Right List"/>
</node>
<node CREATED="1305639493924" ID="ID_1214232412" MODIFIED="1306848199421" TEXT="settings">
<node CREATED="1305639500676" ID="ID_985772700" MODIFIED="1305639533881" TEXT="CLUSTER_ANNOTATION_PROTEIN_STAT_COUNT_WORKFLOWS_MIN_VALUE&#xa;change to relative value"/>
</node>
<node CREATED="1306310819443" ID="ID_412782005" MODIFIED="1306310826274" TEXT="Cluster Annotation">
<node CREATED="1306310826955" ID="ID_1722789888" MODIFIED="1306310959075" TEXT="Filtering: all peptide types for cluster exclusion"/>
<node CREATED="1306310845651" ID="ID_1190359007" MODIFIED="1306310986947" TEXT="capture filtering statistics"/>
</node>
<node CREATED="1306847607612" ID="ID_344848758" MODIFIED="1306847612474" TEXT="Normalization">
<node CREATED="1306847613197" ID="ID_1012563806" MODIFIED="1306847642310" TEXT="equalizing sum of intensities per run">
<icon BUILTIN="button_ok"/>
</node>
<node CREATED="1306847645373" ID="ID_571003644" MODIFIED="1306847716692" TEXT="Normalizing by Inten or RT destroys Equalizing"/>
<node CREATED="1307618499003" ID="ID_811293537" MODIFIED="1307618516650" TEXT="normalize by reference run">
<icon BUILTIN="button_ok"/>
</node>
</node>
<node CREATED="1306847790836" ID="ID_1382223968" MODIFIED="1306848195475" TEXT="Clustering">
<node CREATED="1306847794031" ID="ID_1328886192" MODIFIED="1306847976409" TEXT="unclean clustering"/>
</node>
</node>
<node CREATED="1306848216998" HGAP="18" ID="ID_1386041155" MODIFIED="1306848252961" POSITION="right" TEXT="fixed" VSHIFT="36">
<node CREATED="1305644219124" ID="ID_762935393" MODIFIED="1305644235549" TEXT="Alignment:">
<node CREATED="1305644242086" ID="ID_461760651" MODIFIED="1306848149347" TEXT="check RT columns (==0)">
<icon BUILTIN="button_ok"/>
<node CREATED="1305644255307" ID="ID_1018374676" MODIFIED="1305644262773" TEXT="process"/>
<node CREATED="1305644263182" ID="ID_877757409" MODIFIED="1305644267437" TEXT="skip"/>
</node>
</node>
<node CREATED="1303910129460" ID="ID_585850096" MODIFIED="1305715684587" TEXT="Plugin Batch Execution">
<node CREATED="1303910156589" ID="ID_537916125" MODIFIED="1306848163184" TEXT="wait for precursor plugin if set">
<icon BUILTIN="button_ok"/>
</node>
<node CREATED="1303910176621" ID="ID_1749404744" MODIFIED="1306848171730" TEXT="notify plugin execution listener">
<icon BUILTIN="button_ok"/>
</node>
<node CREATED="1303910216202" ID="ID_533866277" MODIFIED="1306848176262" TEXT="wrap by Pipeline-Plugin">
<icon BUILTIN="button_ok"/>
</node>
</node>
<node CREATED="1301498463632" ID="ID_1864796295" MODIFIED="1305715674920" TEXT="stdout/stderr">
<node CREATED="1301498491840" ID="ID_1388307162" MODIFIED="1301646023558" TEXT="synchronize">
<icon BUILTIN="button_ok"/>
<node CREATED="1301645938339" ID="ID_1626207668" MODIFIED="1301646019955" TEXT="SynchronizedPrintStream"/>
</node>
<node CREATED="1301498498680" ID="ID_708386848" MODIFIED="1306848241746" TEXT="separate">
<icon BUILTIN="help"/>
</node>
<node CREATED="1301645143715" ID="ID_1394640388" MODIFIED="1301645161271" TEXT="single stream for both">
<icon BUILTIN="button_ok"/>
</node>
</node>
</node>
</node>
</map>
