-- Create favorites table
CREATE TABLE IF NOT EXISTS favorites (
    idFavorite INT AUTO_INCREMENT PRIMARY KEY,
    idForum INT NOT NULL,
    idUser INT NOT NULL,
    dateAdded TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (idForum) REFERENCES forum(idForum) ON DELETE CASCADE,
    FOREIGN KEY (idUser) REFERENCES user(idUser) ON DELETE CASCADE,
    UNIQUE KEY unique_favorite (idForum, idUser)
);
