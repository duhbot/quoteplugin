package org.duh102.duhbot.quoteplugin;

import java.util.*;

import org.pircbotx.hooks.*;
import org.pircbotx.hooks.events.*;

import org.duh102.duhbot.functions.*;

public class QuotePlugin extends ListenerAdapter implements ListeningPlugin
{
  static QDBMinder minder = null;
  static {
    minder = new QDBMinder();
    minder.createTables();
  }

  public QuotePlugin()
  {
  }
  
  public ListenerAdapter getAdapter()
  {
    return this;
  }
  public Map<String,String> getHelpFunctions()
  {
    Map<String,String> helpFunctions = new HashMap<String,String>();
    helpFunctions.put(".quote", "read random quote");
    helpFunctions.put(".quote all", "read random quote from all channels");
    helpFunctions.put(".quote x", "read specific quote");
    helpFunctions.put(".quote add [quote]", "add a quote");
    helpFunctions.put(".quote total", "return number of quotes");
    helpFunctions.put(".quote authortotal [author]", "return number of quotes from that author");
    helpFunctions.put(".quote authorlist (author)", "return a list of all authors in the DB or all quotes by a specific author");
    helpFunctions.put(".quote search [query]", "search for quotes, a & b would search for quotes with both a and b");
    helpFunctions.put(".quote find [query]", "search for quotes, a & b would search for quotes with both a and b");
    
    return helpFunctions;
  }
  public String getPluginName()
  {
    return "quotes";
  }
  
//#################### onAction handlers ######################
  public static String message;
  public void onMessage(MessageEvent event)
  {
    message = org.pircbotx.Colors.removeFormattingAndColors(event.getMessage()).trim();
    if(message.startsWith(".quote"))
    {
      doQuote(event);
    }
  }
  
//#################### bot actions ######################
  private void doQuote(MessageEvent event)
  {
    String sender = event.getUser().getNick();
    String channel = event.getChannel().getName();
    String server = event.getBot().getServerInfo().getServerName();
    String[] parts = message.split("\\s+");
    if(parts.length > 1)
    {
      if(parts[1].equalsIgnoreCase("all"))
      {
        Quote quote = minder.getQuote(channel, server, true);
        if(quote != null)
        {
          respond(event, "#" + quote.getQuoteID() + " | Submitted: " +quote.getAuthor() + " | Date: " + quote.getTimestamp().toString());
          respond(event, "" + quote.getQuote());
        }
        else
        {
          respond(event, "Could not retrieve quote!");
        }
      }
      //add operation requires being at least voiced, if not something higher
      else if(parts[1].equalsIgnoreCase("add") && !event.getChannel().getNormalUsers().contains(event.getUser()))
      {
        StringBuilder builder = new StringBuilder(parts[2]);
        for(int i = 3; i < parts.length; i++)
        {
          builder.append(" " + parts[i]);
        }
        int id = -1;
        String toAdd = builder.toString();
        if(builder.toString().length() <= 5) {
          respond(event, "Quote too short, must be longer than 5 characters");
        } else if( (id = minder.addQuote(toAdd, sender, channel, server)) >= 0) {
          respond(event, "Successfully added quote #" + id + ".");
        } else {
          respond(event, "Could not add quote!");
        }
      }
      else if(parts[1].equalsIgnoreCase("total"))
      {
        int count = -1;
        if(parts.length > 2 && parts[2].equals("all"))
        {
          count = minder.getNumQuotes();
        }
        else
        {
          count = minder.getNumQuotes(channel, server);
        }
        if(count > 0)
        {
          respond(event, "There are " + count + " quotes in the database.");
        }
        else
        {
          respond(event, "Could not count quotes!");
        }
      }
      else if(parts[1].equalsIgnoreCase("authortotal") && parts.length >= 3)
      {
        int count = -1;
        if((count = minder.getAuthorQuoteCount(parts[2], channel, server)) >= 0)
        {
          respond(event, "There are " + count + " quotes in the database from " + parts[2] + ".");
        }
        else
        {
          respond(event, "Could not count quotes!");
        }
      }
      else if(parts[1].equalsIgnoreCase("authorlist"))
      {
        if(parts.length >= 3)
        {
          ArrayList<Integer> listQuotes = minder.quotesByAuthor(parts[2], channel, server);
          if(listQuotes != null)
          {
            if(listQuotes.size() > 0)
            {
              StringBuilder list = new StringBuilder();
              list.append(listQuotes.get(0));
              int i = 1;
              int limit = Math.min(288, listQuotes.size());
              while(i < limit)
              {
                if(i % 72 == 0)
                {
                  respond(event, "[list] " + list.toString());
                  list = new StringBuilder();
                  list.append(listQuotes.get(i));
                  i++;
                }
                else
                {
                  list.append(", " + listQuotes.get(i));
                  i++;
                }
              }
              respond(event, "[list] " + list.toString());
              if(limit < listQuotes.size())
              {
                respond(event, "List was too long, foreshortened.");
              }
            }
            else
            {
              respond(event, "No quotes by that author.");
            }
          }
          else
          {
            respond(event, "Could not list quotes!");
          }
        }
        else
        {
          ArrayList<String> authors = minder.getAuthors(channel, server);
          if(authors != null)
          {
            if(authors.size() > 0)
            {
              StringBuilder list = new StringBuilder();
              list.append(authors.get(0));
              int i = 1;
              int limit = Math.min(288, authors.size());
              while(i < limit)
              {
                if((list.length() + authors.get(i).length() + 3) < 500)
                {
                  list.append(", " + authors.get(i));
                  i++;
                }
                else
                {
                  respond(event, "[list] " + list.toString());
                  list = new StringBuilder();
                  list.append(authors.get(i));
                  i++;
                }
              }
              respond(event, "[list] " + list.toString());
              if(limit < authors.size())
              {
                respond(event, "List was too long, foreshortened.");
              }
            }
            else
            {
              respond(event, "No authors.");
            }
          }
          else
          {
            respond(event, "Could not list authors!");
          }
        }
      }
      else if((parts[1].equalsIgnoreCase("search") || parts[1].equalsIgnoreCase("find")) && parts.length >= 3)
      {
        if(parts[2].length() > 1)
        {
          ArrayList<Integer> listQuotes = minder.searchQuote(parts, channel, server);
          if(listQuotes != null)
          {
            if(listQuotes.size() > 0)
            {
              StringBuilder list = new StringBuilder();
              list.append(listQuotes.get(0));
              int i = 1;
              int limit = Math.min(288, listQuotes.size());
              while(i < limit)
              {
                if(i % 72 == 0)
                {
                  respond(event, "[list] " + list.toString());
                  list = new StringBuilder();
                  list.append(listQuotes.get(i));
                  i++;
                }
                else
                {
                  list.append(", " + listQuotes.get(i));
                  i++;
                }
              }
              respond(event, "[list] " + list.toString());
              if(limit < listQuotes.size())
              {
                respond(event, "List was too long, foreshortened.");
              }
            }
            else
            {
              respond(event, "No quotes matched your parameters.");
            }
          }
          else
          {
            respond(event, "Could not list quotes!");
          }
        }
        else
        {
          respond(event, "Too short of a string to search for!");
        }
      }
      else
      {
        int num = -1;
        try
        {
          num = Integer.parseInt(parts[1]);
          if(num > 0)
          {
            Quote quote = minder.getQuote(num, channel, server);
            if(quote != null)
            {
              respond(event, "#" + quote.getQuoteID() + " | Submitted: " +quote.getAuthor() + " | Date: " + quote.getTimestamp().toString());
              respond(event, "" + quote.getQuote());
            }
            else
            {
              respond(event, "Could not retrieve quote!");
            }
          }
        }
        catch(NumberFormatException nfe)
        {
          nfe.printStackTrace();
        }
        catch(Throwable e) {
          e.printStackTrace();
        }
      }
    }
    else
    {
      Quote quote = minder.getQuote(channel, server, false);
      if(quote != null)
      {
        respond(event, "#" + quote.getQuoteID() + " | Submitted: " +quote.getAuthor() + " | Date: " + quote.getTimestamp().toString());
        respond(event, "" + quote.getQuote());
      }
      else
      {
        respond(event, "Could not retrieve quote!");
      }
    }
  }
  //#################### Utility quote response ######################
  public void respond(MessageEvent event, String response)
  {
    event.respondChannel("[Q] " + response);
  }
}
