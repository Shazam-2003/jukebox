import javax.sound.sampled.*;
import java.io.File;
import java.sql.*;
import java.util.*;

public class Jukebox {
    private Clip clip;
    private AudioInputStream audioStream;
    private long clipTimePosition;
    private boolean isLooping = false;
    private List<String> currentPlaylistPaths = new ArrayList<>();
    private int currentIndex = -1;

    public static void main(String[] args) {
        Jukebox jukebox = new Jukebox();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            final String URL = "jdbc:mysql://localhost:3306/jukebox3";
            Connection conn = DriverManager.getConnection(URL, "root", "root");
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("\n---------Welcome to Music world---------");
                System.out.println("1. View All Songs");
                System.out.println("2. Create Playlist");
                System.out.println("3. Add Song to Playlist");
                System.out.println("4. View Playlists");
                System.out.println("5. View Songs in Playlist");
                System.out.println("6. Play Playlist");
                System.out.println("7. Pause");
                System.out.println("8. Resume");
                System.out.println("9. Stop");
                System.out.println("10. Shuffle Playlist");
                System.out.println("11. Toggle Loop");
                System.out.println("12. Next Song");git@github.com:Shazam-2003/jukebox3.git
                System.out.println("13. Previous Song");
                System.out.println("0. Exit");
                System.out.print("Enter your choice: ");
                int choice = scanner.nextInt();
                scanner.nextLine();
                switch (choice) {
                    case 1 -> showAllSongs(conn);
                    case 2 -> createPlaylist(conn, scanner);
                    case 3 -> addSongsToPlaylist(conn, scanner);
                    case 4 -> showPlaylists(conn);
                    case 5 -> viewSongsFromPlaylist(conn, scanner);
                    case 6 -> jukebox.playEntirePlaylist(conn, scanner);
                    case 7 -> jukebox.pause();
                    case 8 -> jukebox.resume();
                    case 9 -> jukebox.stop();
                    case 10 -> jukebox.shuffleAndPlay(conn, scanner);
                    case 11 -> jukebox.toggleLoop();
                    case 12 -> jukebox.nextSong();
                    case 13 -> jukebox.previousSong();
                    case 0 -> {
                        jukebox.stop();
                        jukebox.closeAudio();
                        System.out.println("Exiting...");
                        return;
                    }
                    default -> System.out.println("Invalid choice!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void createPlaylist(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter Playlist Name: ");
        String p_name = scanner.nextLine();
        String sql = "INSERT INTO playlist (p_name) VALUES (?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p_name);
            stmt.executeUpdate();
            System.out.println("Playlist created successfully.");
        }
    }

    static void showAllSongs(Connection conn) throws SQLException {
        String sql = "SELECT songID, songTitle, artistName, genre, album, duration FROM songs ORDER BY songID";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.printf("%-5s %-20s %-15s %-10s %-15s %-10s\n", "ID", "Title", "Artist", "Genre", "Album", "Duration");
            while (rs.next()) {
                String duration = rs.getString("duration");
                System.out.printf("%-5d %-20s %-15s %-10s %-15s %s\n",
                        rs.getInt("songID"),
                        rs.getString("songTitle"),
                        rs.getString("artistName"),
                        rs.getString("genre"),
                        rs.getString("album"),
                        duration);
            }
        }
    }

    static void addSongsToPlaylist(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter Playlist ID: ");
        int id = scanner.nextInt();
        scanner.nextLine();
        System.out.print("Enter Song ID: ");
        int songID = scanner.nextInt();
        String sql = "INSERT INTO playlist_songs (playlist_id, song_id) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setInt(2, songID);
            int rows = stmt.executeUpdate();
            if (rows > 0) System.out.println("Song added to playlist.");
            else System.out.println("Song or playlist not found.");
        }
    }

    static void showPlaylists(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM playlist")) {
            System.out.println("\nPlaylists:");
            while (rs.next()) {
                System.out.println(rs.getInt("p_id") + ". " + rs.getString("p_name"));
            }
        }
    }

    static void viewSongsFromPlaylist(Connection conn, Scanner sc) throws SQLException {
        System.out.print("Enter playlist ID: ");
        int pid = sc.nextInt();
        String sql = """
                SELECT s.songID, s.songTitle, s.album, s.duration
                FROM songs s
                JOIN playlist_songs ps ON s.songID = ps.song_id
                WHERE ps.playlist_id = ?;
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pid);
            ResultSet rs = ps.executeQuery();
            System.out.printf("%-5s %-25s %-20s %-10s\n", "ID", "Title", "Album", "Duration");
            while (rs.next()) {
                String duration = rs.getString("duration");
                System.out.printf("%-5d %-25s %-20s %s\n",
                        rs.getInt("songID"),
                        rs.getString("songTitle"),
                        rs.getString("album"),
                        duration);
            }
        }
    }

    void playEntirePlaylist(Connection conn, Scanner sc) throws SQLException {
        System.out.print("Enter playlist ID: ");
        int pid = sc.nextInt();
        currentPlaylistPaths.clear();
        currentIndex = 0;
        String sql = """
                SELECT filePath FROM songs s
                JOIN playlist_songs ps ON s.songID = ps.song_id
                WHERE ps.playlist_id = ?;
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pid);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                currentPlaylistPaths.add(rs.getString("filePath"));
            }
        }
        if (currentPlaylistPaths.isEmpty()) {
            System.out.println("No songs in this playlist.");
            return;
        }
        playCurrentSong();
    }

    void playCurrentSong() {
        if (currentIndex >= 0 && currentIndex < currentPlaylistPaths.size()) {
            String path = currentPlaylistPaths.get(currentIndex);
            System.out.println("Now Playing: " + path);
            loadAudio(path);
        }
    }

    void nextSong() {
        if (currentIndex < currentPlaylistPaths.size() - 1) {
            currentIndex++;
            playCurrentSong();
        } else {
            System.out.println("Reached end of playlist.");
        }
    }

    void previousSong() {
        if (currentIndex > 0) {
            currentIndex--;
            playCurrentSong();
        } else {
            System.out.println("Already at the first song.");
        }
    }

    void shuffleAndPlay(Connection conn, Scanner sc) throws SQLException {
        System.out.print("Enter playlist ID: ");
        int pid = sc.nextInt();
        List<String> paths = new ArrayList<>();
        String sql = """
                SELECT filePath FROM songs s
                JOIN playlist_songs ps ON s.songID = ps.song_id
                WHERE ps.playlist_id = ?;
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pid);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) paths.add(rs.getString("filePath"));
        }
        Collections.shuffle(paths);
        for (String path : paths) {
            loadAudio(path);
        }
    }

    void toggleLoop() {
        isLooping = !isLooping;
        if (clip != null) clip.loop(isLooping ? Clip.LOOP_CONTINUOUSLY : 0);
        System.out.println("Looping is now " + (isLooping ? "ON" : "OFF"));
    }

    void loadAudio(String filePath) {
        try {
            File audioFile = new File(filePath);
            if (audioFile.exists()) {
                if (clip != null && clip.isOpen()) clip.close();
                audioStream = AudioSystem.getAudioInputStream(audioFile);
                clip = AudioSystem.getClip();
                clip.open(audioStream);
                clip.start();
                if (isLooping) clip.loop(Clip.LOOP_CONTINUOUSLY);
                System.out.println("Audio loaded and playing.");
            } else {
                System.out.println("❌ File not found: " + filePath);
            }
        } catch (Exception e) {
            System.out.println("Error loading audio.");
            e.printStackTrace();
        }
    }

    void pause() {
        if (clip != null && clip.isRunning()) {
            clipTimePosition = clip.getMicrosecondPosition();
            clip.stop();
            System.out.println("Paused.");
        }
    }

    void resume() {
        if (clip != null && !clip.isRunning()) {
            clip.setMicrosecondPosition(clipTimePosition);
            clip.start();
            System.out.println("Resumed.");
        }
    }

    void stop() {
        if (clip != null) {
            clip.stop();
            clip.setMicrosecondPosition(0);
            System.out.println("Stopped.");
        }
    }

    void closeAudio() {
        if (clip != null) {
            clip.close();
            System.out.println("Closed.");
        }
    }
}
