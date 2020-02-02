package org.duh102.duhbot.quoteplugin;

import java.util.*;

public class QDBMinder
{
  public QDBMinder()
  {
    
  }
  
  public synchronized ArrayList<String> getAuthors(String channel, String server)
  {
    return QuoteDB.getAuthors();
  }
  public synchronized ArrayList<Integer> searchQuote(String[] toSearch, String channel, String server)
  {
    return QuoteDB.searchQuote(toSearch);
  }
  public synchronized ArrayList<Integer> quotesByAuthor(String author, String channel, String server)
  {
    return QuoteDB.quotesByAuthor(author);
  }
  public synchronized int addQuote(String quote, String author, String channel, String server)
  {
    return QuoteDB.addQuote(quote, author, channel, server);
  }
  public synchronized int addQuoteTime(String quote, String author, long time, String channel, String server)
  {
    return QuoteDB.addQuoteTime(quote, author, time, channel, server);
  }
  public synchronized Quote getQuote(int quoteID, String channel, String server)
  {
    return QuoteDB.getQuote(quoteID, channel);
  }
  public synchronized Quote getQuote(String channel, String server, boolean all)
  {
    return QuoteDB.getQuote(channel, all);
  }
  public synchronized int getAuthorQuoteCount(String name, String channel, String server)
  {
    return QuoteDB.getAuthorQuoteCount(name);
  }
  public synchronized ArrayList<Integer> getListQuotesByAuthor(String name, String channel, String server)
  {
    return QuoteDB.getListQuotesByAuthor(name);
  }
  public synchronized int getNumQuotes(String channel, String server)
  {
    return QuoteDB.getNumQuotes(channel);
  }
  public synchronized int getNumQuotes()
  {
    return QuoteDB.getNumQuotes();
  }
  public synchronized void createTables()
  {
    QuoteDB.createTables();
  }
  public synchronized  Quote getLastQuote(String channel, String server) { return QuoteDB.getLastQuote(channel); }
  public synchronized  Quote getLastQuote() { return QuoteDB.getLastQuote(null); }
}