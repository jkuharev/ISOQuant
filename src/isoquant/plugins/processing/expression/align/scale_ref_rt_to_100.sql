SET @scaleFactor := 1;
select
	@scaleFactor := 100/min(grl)
from
	(
		SELECT
			max(ref_rt) as grl
		FROM
			`rtw`
		GROUP BY run
	) as  x;
	
UPDATE rtw SET ref_rt = ref_rt * @scaleFactor;