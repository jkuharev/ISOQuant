-- @ : resetting EMRT intensities ...
-- overwrite old cor_inten values
UPDATE `clustered_emrt` SET cor_inten=inten;
OPTIMIZE TABLE `clustered_emrt`;
-- @ : resetting EMRT intensities ... [done]