DROP DATABASE IF EXISTS jukebox3;
CREATE DATABASE IF NOT EXISTS jukebox3;
USE jukebox3;

CREATE TABLE IF NOT EXISTS songs (
    songID INT AUTO_INCREMENT PRIMARY KEY, 
    songTitle VARCHAR(50) NOT NULL,
    filePath VARCHAR(500) NOT NULL, 
    artistName VARCHAR(50) NOT NULL, 
    genre VARCHAR(50) NOT NULL,
    album VARCHAR(50),
    duration FLOAT
);

INSERT INTO songs (songTitle, filePath, artistName, genre, album, duration) 
VALUES
('hyper', 'C:\\Users\\vaibh\\Desktop\\New folder\\hyper.wav', 'techno', 'pop', 'Techno Hits', 2.03),
('vinyl', 'C:\\Users\\vaibh\\Desktop\\New folder\\vinyl.wav', 'techno', 'modern jazz', 'Jazz Vibes', 2.44),
('amapiono', 'C:\\Users\\vaibh\\Desktop\\New folder\\amapiano.wav', 'techno', 'beat', 'Beat Masters', 2.20),
('islandy', 'C:\\Users\\vaibh\\Desktop\\New folder\\islandy.wav', 'blade', 'pop', 'Techno Hits', 0.25),
('pluck', 'C:\\Users\\vaibh\\Desktop\\New folder\\pluck.wav', 'blade', 'modern jazz', 'Jazz Vibes', 0.24),
('run', 'C:\\Users\\vaibh\\Desktop\\New folder\\run.wav', 'blade', 'beat', 'Beat Masters', 0.33),
('ultimatum', 'C:\\Users\\vaibh\\Desktop\\New folder\\ultimatum.wav', 'blade', 'pop', 'Techno Hits', 0.34);

CREATE TABLE IF NOT EXISTS playlist (
    p_id INT PRIMARY KEY AUTO_INCREMENT, 
    p_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS playlist_songs (
    playlist_id INT NOT NULL,
    song_id INT NOT NULL,
    FOREIGN KEY (playlist_id) REFERENCES playlist(p_id) ON DELETE CASCADE,
    FOREIGN KEY (song_id) REFERENCES songs(songID) ON DELETE CASCADE,
    PRIMARY KEY (playlist_id, song_id)
);

SELECT * FROM playlist;
SELECT * FROM songs ORDER BY songID ASC;
