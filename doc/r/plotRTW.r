library(RMySQL)

######################################## configuration variables
plotWidth=600
plotHeight=400
currentProject="Proj__12465518426020_3564317579743981_268_1"

# do all encapsulated in main function
main=function()
{
	dbDrv=dbDriver("MySQL")
	dbCon=dbConnect(dbDrv,host="localhost",dbname=currentProject,user="root",pass="")

	workflows = getWorkflowList(dbCon)
	
	makeColors( length(workflows) )
	
	for(i in 1:length(workflows))
	{
		plot4Run(workflows[i], dbCon);
	}
	dbDisconnect(dbCon)
	dbUnloadDriver(dbDrv)
}

######################################## define functions
# divide a vector into given number of parts
# @param values the vector to divide
# @param parts number of parts
# @return list-object with given parts from given vector
getParts=function(values, parts)
{
	n = length(values)
	step = as.integer(n / parts)
	i=1
	results=list()
	currentIndex = 1
	while( i<n )
	{
		lower = i
		upper = i + step
		if(upper>n) upper = n
		win = values[lower:upper]
		if(i==1) 
		{
			currentIndex = 1 
		}
		else 
		{
			currentIndex = currentIndex + 1 
		}
		results[currentIndex] = list(win)
		i = upper + 1
	}
	return(results)
}

# Workflows zum projekt auflisten
getWorkflowList=function(dbCon)
{
	sql="SELECT `index` FROM workflow ORDER BY `index` ASC"
	dbRes=dbGetQuery(dbCon, sql)
	return(dbRes[,1])
}

getRefRun=function(dbCon)
{
	sql="SELECT `workflow_index`, count(`index`) as nEMRTs FROM clustered_emrt GROUP BY workflow_index ORDER BY nEMRTs DESC LIMIT 1"
	dbRes=dbGetQuery(dbCon, sql)
	return(dbRes[1,1])
}

colors = topo.colors(32, alpha=1)

makeColors=function(numberOfColors)
{
	colors =  topo.colors(numberOfColors, alpha=1)
	cat(sprintf("creating %i colors ...", numberOfColors))
}

getColor=function(colorIndex)
{
	return( colors[colorIndex] )
}

# Workflow-Daten als PNG-Bilder ausgeben
plot4Run=function(runIndex, dbCon)
{
	sql=sprintf("SELECT plgs_ref_rt, ref_rt FROM clustered_emrt WHERE workflow_index=%i ORDER BY ref_rt ASC", runIndex)
	dbRes=dbGetQuery(dbCon, sql)

	ret=dbRes[,1]
	ref_rt=dbRes[,2]
	if(runIndex==1)
	{
		plot(ref_rt, ret, pch=20, cex=0.1, main="time warping", xlab="aligned time", ylab="original time", col=getColor(runIndex))
	}
	else
	{
		points(ref_rt, ret, pch=20, cex=0.1, main="time warping", xlab="aligned time", ylab="original time", col=getColor(runIndex))

	}
}
	
main()
#