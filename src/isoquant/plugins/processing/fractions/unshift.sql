-- @ unshifting fractions ...
UPDATE 
	clustered_emrt as ce 
	JOIN mass_spectrum as ms 
	USING(low_energy_index)
SET
	ce.rt=oldRT, 
	ce.ref_rt= ( ce.ref_rt - ( ms.RT - ms.oldRT ) )
;

UPDATE mass_spectrum SET RT=oldRT;

ALTER  TABLE `mass_spectrum` DROP COLUMN `oldRT`;
-- @ unshifting fractions ... [done]