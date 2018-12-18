package org.duh102.duhbot.quoteplugin;


public class Quote
{
  private int quoteID;
  private String quote;
  private String author;
  private String channel;
  private String server;
  private java.sql.Timestamp timestamp;
  
  public Quote(int quoteID, String channel, String server, String quote, String author, java.sql.Timestamp timestamp)
  {
    this.quoteID = quoteID;
    this.channel = channel;
    this.server = server;
    this.quote = quote;
    this.author = author;
    this.timestamp = timestamp;
  }
  
  public int getQuoteID()
  {
    return quoteID;
  }
  public String getQuote()
  {
    return quote;
  }
  public String getAuthor()
  {
    return author;
  }
  public String getChannel()
  {
    return channel;
  }
  public String getServer()
  {
    return server;
  }
  public java.sql.Timestamp getTimestamp()
  {
    return timestamp;
  }
  public String toString() {
    return String.format("Quote(%d, %s, %s, \"%s\", %s, %s)", quoteID, channel, server, quote, author, timestamp.toString());
  }
}
