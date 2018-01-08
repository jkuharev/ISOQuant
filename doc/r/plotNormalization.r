library(RMySQL)


######################################################################
# define + for String concatenation
"+"=function(...) UseMethod("+")
"+.default"=.Primitive("+")
"+.character"=function(...) paste(...,sep="")
######################################################################

######################################## configuration variables
plotWidth=1200
plotHeight=800

currentProject="Proj__13178037643940_7725542871365159_IQ_1"
# currentProject="Proj__13178037643940_7725542871365159_IQ_1"


# do all encapsulated in main function
main=function()
{
	dbDrv=dbDriver("MySQL")
	dbCon=dbConnect(dbDrv,host="localhost",port=3307,dbname=currentProject,user="root",pass="")

	dbGetQuery(dbCon, "DROP TABLE IF EXISTS ca")
	dbGetQuery(dbCon, 
	"
	CREATE TABLE ca
	SELECT 
		`cluster_average_index`,
		COUNT( DISTINCT workflow_index ) as cluster_size,
		AVG(rt) as `ave_rt`,
		AVG(ref_rt) as `ave_ref_rt`,
		AVG(`mass`) as ave_mass,
		AVG(`inten`) as ave_inten,
		AVG(`cor_inten`) as ave_cor_inten
	FROM
		clustered_emrt
	GROUP BY
		`cluster_average_index`
	")
	dbGetQuery(dbCon, "ALTER TABLE ca ADD PRIMARY KEY(cluster_average_index)")
	
	dbGetQuery(dbCon, "OPTIMIZE TABLE clustered_emrt")
	dbGetQuery(dbCon, "OPTIMIZE TABLE ca")
	
	workflows = getWorkflowList(dbCon)
	for( i in 1:length(workflows) )
	{
		plotWorkflowReport(workflows[i], dbCon);
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
	sql="SELECT `index` FROM workflow"
	dbRes=dbGetQuery(dbCon, sql)
	return(dbRes[,1])
}

# Workflow-Daten als PNG-Bilder ausgeben
plotWorkflowReport=function(workflow, dbCon)
{
	cat("plotting graphics for run "+ workflow + " . . .\n")
	
	filePrefix=sprintf("graphics/%i",workflow)
	dir.create("graphics")
	
	lowessBW = 0.25
	ratioLimits = c(-2, 2)
	pointSize = .75
	intLimits = c(10, 16)
	retLimits = c(25, 120)
	massLimits= c(500, 4000)
	
	#png(filename=sprintf("%s_%s",filePrefix,"intensity_%i.png"),width=plotWidth, height=plotHeight, units="px",pointsize=12, bg="white")
	# pdf(file=filePrefix + "_%i.pdf", onefile=FALSE, width=8*2, height=6*3)
	
	#pdf(file=filePrefix + ".pdf", onefile=FALSE, width=6*2, height=3*3)
	png(filename=filePrefix + ".png", width=plotWidth, height=plotHeight, units="px",pointsize=12, bg="white")
	
	par(mfrow=c(3,2))
	
	dbRes=dbGetQuery(dbCon, "
		SELECT
			`inten`, `cor_inten`, ave_inten, ave_cor_inten, `rt`, ref_rt
		FROM
			`clustered_emrt` JOIN ca USING(`cluster_average_index`)
		WHERE
			`workflow_index`="+workflow+" AND cluster_size > 1
		ORDER BY 
			inten ASC
	")
	
	lint = log2( dbRes[,1] )
	lcint = log2( dbRes[,2] )
	lr = lint - log2( dbRes[,3] )
	clr = lcint - log2( dbRes[,4] )
	ret = dbRes[,5]
	ref_ret = dbRes[,6]
	
	l2rLabel = expression( paste( log[2],"  ", frac(intensity, cluster_average_intensity) ) )
	l2iLabel = expression( paste( log[2]( intensity ) ) )
	
	par( mar=c( 6, 8, .2, .2 ) )
	par( cex.lab=1.2 )
	
	plot(
		lint, 
		lr,
		pch=20,
		cex=pointSize,
		main="", #"original data",
		xlab=l2iLabel,
		ylab=l2rLabel, #"log2( intensity / cluster_average_intensity )",
		ylim=ratioLimits,
		xlim=intLimits
	)
	grid()
	#lines(lowess(lr~lint, f=lowessBW), col="red")

	plot(
		lint, 
		clr,
		pch=20,
		cex=pointSize,
		main="", #"normalized data",
		xlab=l2iLabel,
		ylab=l2rLabel, #"log2( intensity / cluster_average_intensity )",
		ylim=ratioLimits,
		xlim=intLimits
	)
	grid()
	#lines( lowess(clr~lcint, f=lowessBW), col="red" )

	dbRes=dbGetQuery(dbCon, "
		SELECT
			`inten`, `cor_inten`, ave_inten, ave_cor_inten, `rt`, ref_rt
		FROM
			`clustered_emrt` JOIN ca USING(`cluster_average_index`)
		WHERE
			`workflow_index`="+workflow+" AND cluster_size > 1
		ORDER BY 
			ref_rt ASC
	")
	
	lint = log2( dbRes[,1] )
	lcint = log2( dbRes[,2] )
	lr = lint - log2( dbRes[,3] )
	clr = lcint - log2( dbRes[,4] )
	ret = dbRes[,5]
	ref_ret = dbRes[,6]

	plot(
		ref_ret, 
		lr,
		pch=20,
		cex=pointSize,
		main="", #"original data",
		xlab="retention time [min]",
		ylab=l2rLabel, #"log2( intensity / cluster_average_intensity )",
		ylim=ratioLimits,
		xlim=retLimits
	)
	grid()
	#lines(lowess(lr~ref_ret, f=lowessBW), col="red")

	plot(
		ret, 
		clr,
		pch=20,
		cex=pointSize,
		main="", #"normalized data",
		xlab="retention time [min]",
		ylab=l2rLabel,
		ylim=ratioLimits,
		xlim=retLimits
	)
	grid()
	#lines( lowess(clr~ret, f=lowessBW), col="red" )

	dbRes=dbGetQuery(dbCon, "
		SELECT
			`inten`, `cor_inten`, ave_inten, ave_cor_inten, mass
		FROM
			`clustered_emrt` JOIN ca USING(`cluster_average_index`)
		WHERE
			`workflow_index`="+workflow+" AND cluster_size > 1
		ORDER BY 
			mass ASC
	")
	
	lint = log2( dbRes[,1] )
	lcint = log2( dbRes[,2] )
	lr = lint - log2( dbRes[,3] )
	clr = lcint - log2( dbRes[,4] )
	mass = dbRes[,5]

	plot(
		mass, 
		lr,
		pch=20,
		cex=pointSize,
		main="", #"original data",
		xlab="mass [Da]",
		ylab=l2rLabel,
		ylim=ratioLimits,
		xlim=massLimits
	)
	grid()
	#lines(lowess(lr~ref_ret, f=lowessBW), col="red")

	plot(
		mass, 
		clr,
		pch=20,
		cex=pointSize,
		main="", #"normalized data",
		xlab="mass [Da]",
		ylab=l2rLabel,
		ylim=ratioLimits,
		xlim=massLimits
	)
	grid()
	#lines( lowess(clr~ret, f=lowessBW), col="red" )

	dev.off()
}

# call main function
main()

