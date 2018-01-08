require(RMySQL)

################################################################################
# define + for String concatenation
"+"=function(...) UseMethod("+")
"+.default"=.Primitive("+")
"+.character"=function(...) paste(...,sep="")
################################################################################

dbHost="127.0.0.1"
dbPort=3307
dbUser="root"
dbPass=""
dbName = "Proj__13554790738200_199487011444631_100_7"

dbDrv=dbDriver("MySQL")
dbCon=dbConnect(dbDrv,host=dbHost, port=dbPort, dbname=dbName, user=dbUser, pass=dbPass)

sql = "
SELECT 
  `mass_error_ppm`
FROM 
  peptide JOIN best_peptides USING(sequence, modifier) 
GROUP BY 
  sequence, modifier, workflow_index
"
dbRes = dbGetQuery(dbCon, sql)

error_ppm = dbRes$mass_error_ppm

dbDisconnect(dbCon)
dbUnloadDriver(dbDrv)

plot(density(error_ppm))
cat( "mean of absolute error is " + sqrt(mean(error_ppm^2)) )