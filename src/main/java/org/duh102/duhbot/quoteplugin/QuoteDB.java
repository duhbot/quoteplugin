package org.duh102.duhbot.quoteplugin;

import java.sql.*;
import java.util.*;

public class QuoteDB {
    private static String dbfile = "quotes.db";

    public static ArrayList<String> getAuthors() {
        ArrayList<String> authors = new ArrayList<String>();
        Connection conn = getDBConnection();
        if (conn != null) {
            try {
                PreparedStatement prep = conn.prepareStatement("select distinct author from quote order by author collate nocase asc;");
                ResultSet rs = prep.executeQuery();
                while (rs.next()) {
                    authors.add(rs.getString("author"));
                }
                rs.close();
                return authors;
            } catch (java.sql.SQLException sqle) {
                sqle.printStackTrace();
                return null;
            } finally {
                try {
                    conn.close();
                } catch (java.sql.SQLException sqle) {
                    sqle.printStackTrace();
                    return null;
                }
            }
        } else {
            return null;
        }
    }

    public static ArrayList<Integer> searchQuote(String[] toSearch) {
        //search strings come in an array and the first two words are .quote and search
        //after that are the search parameters, the only extra character being &
        //.quote search red & white would return the quotes that have both red AND white in them
        if (toSearch.length > 2) {
            ArrayList<Integer> quotes = new ArrayList<Integer>();
            ArrayList<String> searchParams = new ArrayList<String>();
            StringBuilder searchString = new StringBuilder("select quoteid from quote where ");
            StringBuilder searchParam = new StringBuilder("");

            for (int i = 2; i < toSearch.length; i++) {
                if (!toSearch[i].equals("&")) {
                    searchParam.append(" ");
                    searchParam.append(toSearch[i]);
                } else {
                    if (searchParams.size() > 0) {
                        searchString.append(" AND");
                    }
                    searchString.append(" quote like ?");
                    searchParams.add(searchParam.toString().trim());
                    searchParam.setLength(0);
                }
            }
            if (searchParam.length() > 0) {
                if (searchParams.size() > 0) {
                    searchString.append(" AND");
                }
                searchString.append(" quote like ?");
                searchParams.add(searchParam.toString().trim());
            }
            searchString.append(";");

            Connection conn = getDBConnection();
            if (conn != null) {
                try {
                    PreparedStatement prep = conn.prepareStatement(searchString.toString());
                    for (int i = 0; i < searchParams.size(); i++) {
                        prep.setString(i + 1, "%" + searchParams.get(i) + "%");
                    }
                    ResultSet rs = prep.executeQuery();
                    while (rs.next()) {
                        quotes.add(rs.getInt("quoteid"));
                    }
                    rs.close();
                    return quotes;
                } catch (java.sql.SQLException sqle) {
                    sqle.printStackTrace();
                    return null;
                } finally {
                    try {
                        conn.close();
                    } catch (java.sql.SQLException sqle) {
                        sqle.printStackTrace();
                        return null;
                    }
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public static ArrayList<Integer> quotesByAuthor(String author) {
        ArrayList<Integer> quotes = new ArrayList<Integer>();
        Connection conn = getDBConnection();
        if (conn != null) {
            try {
                PreparedStatement prep = conn.prepareStatement("select quoteid from quote where author = ?;");
                prep.setString(1, author);
                ResultSet rs = prep.executeQuery();
                while (rs.next()) {
                    quotes.add(rs.getInt("quoteid"));
                }
                rs.close();
                return quotes;
            } catch (java.sql.SQLException sqle) {
                sqle.printStackTrace();
                return null;
            } finally {
                try {
                    conn.close();
                } catch (java.sql.SQLException sqle) {
                    sqle.printStackTrace();
                    return null;
                }
            }
        } else {
            return null;
        }
    }

    public static int addQuote(String quote, String author, String channel, String server) {
        long now = System.currentTimeMillis();
        return addQuoteTime(quote, author, now, channel, server);
    }

    public static int addQuoteTime(String quote, String author, long time, String channel, String server) {
        Connection conn = getDBConnection();
        if (conn != null) {
            try {
                PreparedStatement prep = conn.prepareStatement("insert into quote (channel, server, quote, author, timestamp) values (?, ?, ?, ?, ?)");
                prep.setString(1, channel);
                prep.setString(2, server);
                prep.setString(3, quote);
                prep.setString(4, author);
                prep.setLong(5, time);
                int i = prep.executeUpdate();
                if (i > 0) {
                    prep = conn.prepareStatement("select quoteid from quote where timestamp = ?;");
                    prep.setLong(1, time);
                    ResultSet rs = prep.executeQuery();
                    int quoteID = -1;
                    quoteID = rs.getInt("quoteid");
                    rs.close();
                    return quoteID;
                } else {
                    conn.close();
                    return -1;
                }
            } catch (java.sql.SQLException sqle) {
                sqle.printStackTrace();
                return -1;
            } finally {
                try {
                    conn.close();
                } catch (java.sql.SQLException sqle) {
                    sqle.printStackTrace();
                    return -1;
                }
            }
        } else {
            return -1;
        }
    }

    public static Quote getQuote(int quoteID, String channel) {
        Connection conn = getDBConnection();
        if (conn != null) {
            try {
                PreparedStatement prep = conn.prepareStatement("select * from quote where quoteid = ?");
                prep.setInt(1, quoteID);
                ResultSet rs = prep.executeQuery();
                int gQuoteID = -1;
                String gServer = "";
                String gChannel = "";
                String quote = "";
                String author = "";
                java.sql.Timestamp timestamp = null;
                if (rs.isClosed()) {
                    return null;
                }
                gQuoteID = rs.getInt("quoteid");
                gChannel = rs.getString("channel");
                gServer = rs.getString("server");
                quote = rs.getString("quote");
                author = rs.getString("author");
                timestamp = new java.sql.Timestamp(rs.getLong("timestamp"));
                rs.close();
                if (gQuoteID > -1) {
                    return new Quote(gQuoteID, gChannel, gServer, quote, author, timestamp);
                }
            } catch (java.sql.SQLException sqle) {
                sqle.printStackTrace();
            } finally {
                try {
                    if (conn != null) {
                        conn.close();
                    }
                } catch (java.sql.SQLException sqle) {
                    sqle.printStackTrace();
                }
            }
        }
        return null;
    }

    public static Quote getQuote(String channel, boolean all) {
        Connection conn = getDBConnection();
        if (conn != null) {
            try {
                String retrieveCode = "SELECT * FROM quote where channel = ? and timestamp > 0 ORDER BY RANDOM() LIMIT 1";
                if (all) {
                    retrieveCode = "SELECT * FROM quote ORDER BY RANDOM() LIMIT 1";
                }
                PreparedStatement prep = conn.prepareStatement(retrieveCode);
                if (!all) {
                    prep.setString(1, channel);
                }
                ResultSet rs = prep.executeQuery();
                int gQuoteID = -1;
                String gServer = "";
                String gChannel = "";
                String quote = "";
                String author = "";
                java.sql.Timestamp timestamp = null;
                gQuoteID = rs.getInt("quoteid");
                gChannel = rs.getString("channel");
                gServer = rs.getString("server");
                quote = rs.getString("quote");
                author = rs.getString("author");
                timestamp = new java.sql.Timestamp(rs.getLong("timestamp"));
                rs.close();
                if (gQuoteID > -1) {
                    return new Quote(gQuoteID, gChannel, gServer, quote, author, timestamp);
                } else {
                    return null;
                }
            } catch (java.sql.SQLException sqle) {
                sqle.printStackTrace();
                return null;
            } finally {
                try {
                    conn.close();
                } catch (java.sql.SQLException sqle) {
                    sqle.printStackTrace();
                    return null;
                }
            }
        } else {
            return null;
        }
    }

    public static Quote getQuoteBetween(String channel, boolean all, int lowerBound, int upperBound) {
        Connection conn = getDBConnection();
        if (conn != null) {
            try {
                String retrieveCode = "SELECT * FROM quote where ";
                if (!all) {
                    retrieveCode += "channel = ? and ";
                }
                retrieveCode += "id > ? and id < ? and timestamp > 0 ORDER BY RANDOM() LIMIT 1";
                PreparedStatement prep = conn.prepareStatement(retrieveCode);
                int arg = 1;
                if (!all) {
                    prep.setString(arg, channel);
                    arg++;
                }
                prep.setInt(arg, lowerBound);
                arg++;
                prep.setInt(arg, upperBound);
                ResultSet rs = prep.executeQuery();
                int gQuoteID = rs.getInt("quoteid");
                String gChannel = rs.getString("channel");
                String gServer = rs.getString("server");
                String quote = rs.getString("quote");
                String author = rs.getString("author");
                java.sql.Timestamp timestamp = new java.sql.Timestamp(rs.getLong("timestamp"));
                rs.close();
                if (gQuoteID > -1) {
                    return new Quote(gQuoteID, gChannel, gServer, quote, author, timestamp);
                } else {
                    return null;
                }
            } catch (java.sql.SQLException sqle) {
                sqle.printStackTrace();
                return null;
            } finally {
                try {
                    conn.close();
                } catch (java.sql.SQLException sqle) {
                    sqle.printStackTrace();
                    return null;
                }
            }
        } else {
            return null;
        }
    }

    public static int getAuthorQuoteCount(String name) {
        Connection conn = getDBConnection();
        if (conn != null) {
            try {
                PreparedStatement prep = conn.prepareStatement("SELECT count(*) FROM quote WHERE author = ?;");
                prep.setString(1, name);
                ResultSet rs = prep.executeQuery();
                int quoteCount = -1;
                quoteCount = rs.getInt("count(*)");
                rs.close();
                return quoteCount;
            } catch (java.sql.SQLException sqle) {
                sqle.printStackTrace();
            } finally {
                try {
                    conn.close();
                } catch (java.sql.SQLException sqle) {
                    sqle.printStackTrace();
                    return -1;
                }
            }
        }
        return -1;
    }

    public static ArrayList<Integer> getListQuotesByAuthor(String name) {
        Connection conn = getDBConnection();
        if (conn != null) {
            //I believe this should actually be implemented?
            //maybe later
            try {
                conn.close();
            } catch (java.sql.SQLException sqle) {
                sqle.printStackTrace();
            }
        }
        return null;
    }

    public static int getNumQuotes(String channel) {
        Connection conn = getDBConnection();
        if (conn != null) {
            try {
                PreparedStatement prep = conn.prepareStatement("SELECT count(*) FROM quote where channel = ?;");
                prep.setString(1, channel);
                ResultSet rs = prep.executeQuery();
                int quoteCount = -1;
                quoteCount = rs.getInt("count(*)");
                rs.close();
                return quoteCount;
            } catch (java.sql.SQLException sqle) {
                sqle.printStackTrace();
            } finally {
                try {
                    conn.close();
                } catch (java.sql.SQLException sqle) {
                    sqle.printStackTrace();
                    return -1;
                }
            }
        }
        return -1;
    }

    public static int getNumQuotes() {
        Connection conn = getDBConnection();
        if (conn != null) {
            try {
                PreparedStatement prep = conn.prepareStatement("SELECT count(*) FROM quote;");
                ResultSet rs = prep.executeQuery();
                int quoteCount = -1;
                quoteCount = rs.getInt("count(*)");
                rs.close();
                return quoteCount;
            } catch (java.sql.SQLException sqle) {
                sqle.printStackTrace();
            } finally {
                try {
                    conn.close();
                } catch (java.sql.SQLException sqle) {
                    sqle.printStackTrace();
                    return -1;
                }
            }
        }
        return -1;
    }

    public static Quote getLastQuote(String channel) {
        Connection conn = getDBConnection();
        if (conn == null) {
            return null;
        }
        try {
            String retrieveCode = "select * from quote where quoteid >= (select max(quoteid) from quote)";
            if (channel != null) {
                retrieveCode = "select * from quote where quoteid >= (select max(quoteid) from quote where channel = ?) and channel = ?";
            }
            PreparedStatement prep = conn.prepareStatement(retrieveCode);
            if (channel != null) {
                prep.setString(1, channel);
                prep.setString(2, channel);
            }
            ResultSet rs = prep.executeQuery();
            int gQuoteID = -1;
            String gServer = "";
            String gChannel = "";
            String quote = "";
            String author = "";
            java.sql.Timestamp timestamp = null;
            gQuoteID = rs.getInt("quoteid");
            gChannel = rs.getString("channel");
            gServer = rs.getString("server");
            quote = rs.getString("quote");
            author = rs.getString("author");
            timestamp = new java.sql.Timestamp(rs.getLong("timestamp"));
            rs.close();
            if (gQuoteID > -1) {
                return new Quote(gQuoteID, gChannel, gServer, quote, author, timestamp);
            } else {
                return null;
            }
        } catch (java.sql.SQLException sqle) {
            sqle.printStackTrace();
            return null;
        } finally {
            try {
                conn.close();
            } catch (java.sql.SQLException sqle) {
                sqle.printStackTrace();
                return null;
            }
        }
    }

    public static void createTables() {
        Connection conn = getDBConnection();
        if (conn != null) {
            try {
                Statement stat = conn.createStatement();
                stat.executeUpdate("create table if not exists quote (quoteid INTEGER PRIMARY KEY AUTOINCREMENT, channel text, server text, quote text, author text, timestamp integer);");
                conn.close();
            } catch (java.sql.SQLException sqle) {
                sqle.printStackTrace();
            }
        }
    }

    public static Connection getDBConnection() {
        Connection conn = null;
        try {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (java.lang.ClassNotFoundException cnfe) {
                cnfe.printStackTrace();
            }
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbfile);
        } catch (java.sql.SQLException sqle) {
            sqle.printStackTrace();
        }
        return conn;
    }
}
