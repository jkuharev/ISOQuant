require(RMySQL)

################################################################################
# define + for String concatenation
"+"=function(...) UseMethod("+")
"+.default"=.Primitive("+")
"+.character"=function(...) paste(...,sep="")
################################################################################

################################################################################
getIQRT = function(dbCon, runIndex=1, minRT=20, maxRT=100 )
{
  sql = "
    SELECT rt FROM `emrt4quant` 
    WHERE (rt BETWEEN "+minRT+" AND "+maxRT+")
    ORDER BY rt ASC
  "
  dbRes = dbGetQuery(dbCon, sql)
  rt = dbRes$rt
  return(rt);
}
################################################################################

################################################################################
getPLGSRT = function(dbCon, runIndex=1, minRT=20, maxRT=100 )
{
  sql = "
    	SELECT
    		retention_time as rt
    	FROM
    		peptide as p 
    		JOIN query_mass as q ON p.query_mass_index = q.`index` 
    		JOIN low_energy as l ON q.low_energy_index = l.`index` 
    	WHERE 
    		p.type != ('IN_SOURCE')
    		AND length(p.sequence) >= 6
        AND (retention_time BETWEEN "+minRT+" AND "+maxRT+")
      GROUP BY l.`index`
    	ORDER BY retention_time ASC
	"
  dbRes = dbGetQuery(dbCon, sql)
  rt = dbRes$rt
  return(rt);
}
################################################################################

pdf(
  file="peptides_per_second.pdf", 
  width=9, height=6
  #, units="cm", res=600,
  # compression="lzw"
)

dbNames = c(
  "Proj__13554790738200_199487011444631_100_28"
  ,"Proj__13554790738200_199487011444631_100_27"
  ,"Proj__13554790738200_199487011444631_100_21"
#  ,"Proj__13554790738200_199487011444631_100_23"
#  ,"Proj__13554790738200_199487011444631_100_25"
)

dbCols = c("blue", "green", "red")
dbTitles = c("MSE", "HDMSE", "UDMSE")
ppsDens = 1:3

dbDrv=dbDriver("MySQL")


mainSize=1.2
fontFamily="Helvetica"
par(mfrow=c(1,1))
par(mar=c(4.2,4,0.2,0.2))
par(las=1)

for(i in 1:length(dbNames))
{
  cat("processing " + dbName + " . . . \n")
  
  dbHost="192.168.1.102"
  dbPort=3306
  dbUser="root"
  dbPass="ykv16"
  dbName = dbNames[i]
  
  dbCon=dbConnect(dbDrv,host=dbHost, port=dbPort, dbname=dbName, user=dbUser, pass=dbPass)
  
  dbRes = dbGetQuery(dbCon, "SELECT title FROM project")
  prjTitle = gsub("\\W", " ", dbRes$title[1] )
  
  fromRT = 20
  toRT = 100
  
  binSize = 1/6
  parts = seq(fromRT, toRT, binSize)
  
  iqHist = hist( getIQRT(dbCon, 1, fromRT, toRT), breaks=parts, plot=F )
  
  x = iqHist$mids
  y = iqHist$counts / binSize / 60 / 3 # 60 seconds, 3 runs
  
  # lxy = lowess(x,y,f=0.01)
  
  if(T)
  if(i<2)
  {
    plot(
      x, y
      , t="l"
      , lwd=2
      , col=dbCols[i]
      , ylim=c( 0, 14)
      , ylab="average( unique sequences / sec )"
      , xlab="retention time [min]"
      # , main=prjTitle
    )
  }
  else
  {
    points( x, y, t="l", lwd=2, col=dbCols[i] )
  }
  
  if(F)
  if(i<2)
    {
      plot(
        density(y)
        , t="l"
        , lwd=2
        , col=dbCols[i]
        , xlim=c( 0.5, 18)
        #, ylab="average( unique sequences / sec )"
        #, xlab="retention time"
        # , main=prjTitle
      )
    }
  else
  {
    points( density(y), t="l", lwd=2, col=dbCols[i] )
  }
  
  # hp = hist( getPLGSRT(dbCon, 1, fromRT, toRT), breaks=parts, plot=F)
  # plgsY = hp$counts / binSize / 60 / 3
  # points(hp$mids, plgsY, t="l", col="red")
  
  dbDisconnect(dbCon)
}
legend("topright", legend=dbTitles, col=dbCols, lty=1, lwd=5, inset=0.01)

dev.off()

dbUnloadDriver(dbDrv)

################################################################################
################################################################################
################################################################################