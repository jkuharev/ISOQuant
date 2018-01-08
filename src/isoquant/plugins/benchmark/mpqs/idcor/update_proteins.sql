-- @ updating protein ids ...
UPDATE 
	protein 
	JOIN xkey 
	ON protein.entry=xkey.id
SET
	protein.entry=xkey.entry,
	protein.accession=xkey.accession,
	protein.description=xkey.description
;

-- @ updating protein info ...
UPDATE 
	protein_info as protein
	JOIN xkey 
	ON protein.entry=xkey.id
SET
	protein.entry=xkey.entry,
	protein.accession=xkey.accession,
	protein.description=xkey.description
;
	