-- @ shifting retention times for fractions ...
ALTER TABLE mass_spectrum ADD COLUMN oldRT FLOAT DEFAULT 0;
UPDATE mass_spectrum SET oldRT=RT;
SET @timeShiftFactor:=0;
SELECT @timeShiftFactor:=FLOOR( CEILING(MAX(RT)/10) * 10 ) FROM `mass_spectrum`;
UPDATE mass_spectrum SET RT=(oldRT+fraction*@timeShiftFactor);
-- @ shifting retention times for fractions ... [done]