-- Add profile_picture column to utilisateur table
USE smoka;

ALTER TABLE utilisateur 
ADD COLUMN profile_picture TEXT DEFAULT NULL;

-- Update existing users with a default avatar (optional)
UPDATE utilisateur 
SET profile_picture = NULL 
WHERE profile_picture IS NULL;
