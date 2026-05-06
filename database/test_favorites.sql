-- Test script to verify favorites table and data
-- Run this to check if favorites are working

-- Check if favorites table exists
SHOW TABLES LIKE 'favorites';

-- Show favorites table structure
DESCRIBE favorites;

-- Show all favorites data
SELECT * FROM favorites;

-- Show favorites with forum details
SELECT 
    f.idFavorite,
    f.dateAdded,
    fav.idForum,
    fav.titre,
    fav.description,
    fav.idUser,
    u.nom,
    u.prenom
FROM favorites f
INNER JOIN forum fav ON f.idForum = fav.idForum
INNER JOIN utilisateur u ON f.idUser = u.idUser
ORDER BY f.dateAdded DESC;

-- Test query for user favorites (user ID 2 = John Doe)
SELECT 
    forum.*,
    favorites.dateAdded as favoriteDate
FROM forum 
INNER JOIN favorites ON forum.idForum = favorites.idForum 
WHERE favorites.idUser = 2 
ORDER BY favorites.dateAdded DESC;
