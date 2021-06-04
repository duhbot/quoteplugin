package org.duh102.duhbot.quoteplugin;

import java.util.*;

import org.pircbotx.hooks.*;
import org.pircbotx.hooks.events.*;

import org.duh102.duhbot.functions.*;

public class QuotePlugin extends ListenerAdapter implements ListeningPlugin {
    static QDBMinder minder = null;

    static {
        minder = new QDBMinder();
        minder.createTables();
    }

    public QuotePlugin() {
    }

    public ListenerAdapter getAdapter() {
        return this;
    }

    public Map<String, String> getHelpFunctions() {
        Map<String, String> helpFunctions = new HashMap<String, String>();
        helpFunctions.put(".quote", "read random quote");
        helpFunctions.put(".quote all", "read random quote from all channels");
        helpFunctions.put(".quote [x]", "read specific quote");
        helpFunctions.put(".quote add [quote]", "add a quote");
        helpFunctions.put(".quote total", "return number of quotes");
        helpFunctions.put(".quote authortotal [author]", "return number of quotes from that author");
        helpFunctions.put(".quote authorlist (author)", "return a list of all authors in the DB or all quotes by a specific author");
        helpFunctions.put(".quote search [query]", "search for quotes, a & b would search for quotes with both a and b");
        helpFunctions.put(".quote find [query]", "search for quotes, a & b would search for quotes with both a and b");
        helpFunctions.put(".quote pick [query]", "like search, but immediately outputs a random quote from the search results");
        helpFunctions.put(".quote last", "read the most recent quote");
        helpFunctions.put(".quote last all", "read the most recent quote from all channels");
        helpFunctions.put(".quote over [x]", "like pick, but limited to quotes with id over specified number");
        helpFunctions.put(".quote under [x]", "like pick, but limited to quotes with id under specified number");
        helpFunctions.put(".quote between [x] [y]", "like pick, but limited to quotes with id between specified numbers");

        return helpFunctions;
    }

    public String getPluginName() {
        return "quotes";
    }

    //#################### onAction handlers ######################
    public static String message;

    public void onMessage(MessageEvent event) {
        message = org.pircbotx.Colors.removeFormattingAndColors(event.getMessage()).trim();
        if (message.startsWith(".quote")) {
            doQuote(event);
        }
    }

    //#################### bot actions ######################
    private void doQuote(MessageEvent event) {
        String sender = event.getUser().getNick();
        String channel = event.getChannel().getName();
        String server = event.getBot().getServerInfo().getServerName();
        String[] parts = message.split("\\s+");
        Quote quote;
        if (parts.length > 1) {
            int count;
            switch (parts[1].toLowerCase()) {
                case ("all"):
                    quote = minder.getQuote(channel, server, true);
                    if (quote != null) {
                        respond(event, "#" + quote.getQuoteID() + " | Submitted: " + quote.getAuthor() + " | Date: " + quote.getTimestamp().toString());
                        respond(event, "" + quote.getQuote());
                    } else {
                        respond(event, "Could not retrieve quote!");
                    }
                    break;
                //add operation requires being at least voiced, if not something higher
                case ("add"):
                    if (event.getChannel().getNormalUsers().contains(event.getUser())) {
                        respond(event, "Must be voiced to add quotes");
                        return;
                    }
                    StringBuilder builder = new StringBuilder(parts[2]);
                    for (int i = 3; i < parts.length; i++) {
                        builder.append(" ").append(parts[i]);
                    }
                    int id = -1;
                    String toAdd = builder.toString();
                    if (builder.toString().length() <= 5) {
                        respond(event, "Quote too short, must be longer than 5 characters");
                    } else if ((id = minder.addQuote(toAdd, sender, channel, server)) >= 0) {
                        respond(event, "Successfully added quote #" + id + ".");
                    } else {
                        respond(event, "Could not add quote!");
                    }
                    break;
                case ("total"):
                    if (parts.length > 2 && parts[2].equalsIgnoreCase("all")) {
                        count = minder.getNumQuotes();
                    } else {
                        count = minder.getNumQuotes(channel);
                    }
                    if (count > 0) {
                        respond(event, "There are " + count + " quotes in the database.");
                    } else {
                        respond(event, "Could not count quotes!");
                    }
                    break;
                case ("authortotal"):
                    if (parts.length < 3) {
                        respond(event, ".quote authortotal requires the author name");
                        return;
                    }
                    if ((count = minder.getAuthorQuoteCount(parts[2], channel, server)) >= 0) {
                        respond(event, "There are " + count + " quotes in the database from " + parts[2] + ".");
                    } else {
                        respond(event, "Could not count quotes!");
                    }
                    break;
                case ("authorlist"):
                    if (parts.length >= 3) {
                        ArrayList<Integer> listQuotes = minder.quotesByAuthor(parts[2], channel, server);
                        if (listQuotes != null) {
                            if (listQuotes.size() > 0) {
                                StringBuilder list = new StringBuilder();
                                list.append(listQuotes.get(0));
                                int i = 1;
                                int limit = Math.min(288, listQuotes.size());
                                while (i < limit) {
                                    if (i % 72 == 0) {
                                        respond(event, "[list] " + list.toString());
                                        list = new StringBuilder();
                                        list.append(listQuotes.get(i));
                                        i++;
                                    } else {
                                        list.append(", " + listQuotes.get(i));
                                        i++;
                                    }
                                }
                                respond(event, "[list] " + list.toString());
                                if (limit < listQuotes.size()) {
                                    respond(event, "List was too long, foreshortened.");
                                }
                            } else {
                                respond(event, "No quotes by that author.");
                            }
                        } else {
                            respond(event, "Could not list quotes!");
                        }
                    } else {
                        ArrayList<String> authors = minder.getAuthors(channel, server);
                        if (authors != null) {
                            if (authors.size() > 0) {
                                StringBuilder list = new StringBuilder();
                                list.append(authors.get(0));
                                int i = 1;
                                int limit = Math.min(288, authors.size());
                                while (i < limit) {
                                    if ((list.length() + authors.get(i).length() + 3) < 500) {
                                        list.append(", " + authors.get(i));
                                        i++;
                                    } else {
                                        respond(event, "[list] " + list.toString());
                                        list = new StringBuilder();
                                        list.append(authors.get(i));
                                        i++;
                                    }
                                }
                                respond(event, "[list] " + list.toString());
                                if (limit < authors.size()) {
                                    respond(event, "List was too long, foreshortened.");
                                }
                            } else {
                                respond(event, "No authors.");
                            }
                        } else {
                            respond(event, "Could not list authors!");
                        }
                    }
                    break;
                case ("search"):
                case ("find"):
                    if (parts.length < 3) {
                        respond(event, ".quote search requires a query");
                        return;
                    }
                    if (parts[2].length() > 1) {
                        ArrayList<Integer> listQuotes = minder.searchQuote(parts, channel, server);
                        if (listQuotes != null) {
                            if (listQuotes.size() > 0) {
                                StringBuilder list = new StringBuilder();
                                list.append(listQuotes.get(0));
                                int i = 1;
                                int limit = Math.min(288, listQuotes.size());
                                while (i < limit) {
                                    if (i % 72 == 0) {
                                        respond(event, "[list] " + list.toString());
                                        list = new StringBuilder();
                                        list.append(listQuotes.get(i));
                                        i++;
                                    } else {
                                        list.append(", " + listQuotes.get(i));
                                        i++;
                                    }
                                }
                                respond(event, "[list] " + list.toString());
                                if (limit < listQuotes.size()) {
                                    respond(event, "List was too long, foreshortened.");
                                }
                            } else {
                                respond(event, "No quotes matched your parameters.");
                            }
                        } else {
                            respond(event, "Could not list quotes!");
                        }
                    } else {
                        respond(event, "Too short of a string to search for!");
                    }
                    break;
                case ("pick"):
                    if (parts.length < 3) {
                        respond(event, ".quote pick requires a query");
                        return;
                    }
                    if (parts[2].length() > 1) {
                        ArrayList<Integer> listQuotes = minder.searchQuote(parts, channel, server);
                        if (listQuotes != null) {
                            if (listQuotes.size() > 0) {
                                quote = minder.getQuote(listQuotes.get(new Random().nextInt(listQuotes.size())), channel, server);
                                respond(event, "#" + quote.getQuoteID() + " | Submitted: " + quote.getAuthor() + " | Date: " + quote.getTimestamp().toString());
                                respond(event, "" + quote.getQuote());
                            } else {
                                respond(event, "No quotes matched your parameters.");
                            }
                        } else {
                            respond(event, "Could not list quotes!");
                        }
                    } else {
                        respond(event, "Too short of a string to search for!");
                    }
                    break;
                case ("last"):
                    if (parts.length > 2 && parts[2].equalsIgnoreCase("all")) {
                        quote = minder.getLastQuote();
                    } else {
                        quote = minder.getLastQuote(channel, server);
                    }
                    if (quote != null) {
                        respond(event, "#" + quote.getQuoteID() + " | Submitted: " + quote.getAuthor() + " | Date: " + quote.getTimestamp().toString());
                        respond(event, "" + quote.getQuote());
                    } else {
                        respond(event, "Could not retrieve quote!");
                    }
                    break;
                case ("over"):
                    if (parts.length < 3) {
                        respond(event, ".quote over requires a number as a lower bound");
                        return;
                    }
                    Integer over = null;
                    try {
                        over = Integer.parseInt(parts[2]);
                    } catch (NumberFormatException nfe) {
                        respond(event, ".quote over requires a number as a lower bound");
                    }
                    if (over > minder.getNumQuotes(channel)) {
                        respond(event, ".quote over can't deal with a lower bound that's larger than the total number of quotes");
                        return;
                    }
                    quote = minder.getQuoteOver(channel, false, over);
                    if (quote != null) {
                        respond(event, "#" + quote.getQuoteID() + " | Submitted: " + quote.getAuthor() + " | Date: " + quote.getTimestamp().toString());
                        respond(event, "" + quote.getQuote());
                    } else {
                        respond(event, "Could not retrieve quote!");
                    }
                    break;
                case ("under"):
                    if (parts.length < 3) {
                        respond(event, ".quote under requires a number as an upper bound");
                        return;
                    }
                    Integer under = null;
                    try {
                        under = Integer.parseInt(parts[2]);
                    } catch (NumberFormatException nfe) {
                        respond(event, ".quote under requires a number as an upper bound");
                    }
                    if (under <= 1) {
                        respond(event, ".quote under requires an upper bound that is larger than 1");
                        return;
                    }
                    quote = minder.getQuoteUnder(channel, false, under);
                    if (quote != null) {
                        respond(event, "#" + quote.getQuoteID() + " | Submitted: " + quote.getAuthor() + " | Date: " + quote.getTimestamp().toString());
                        respond(event, "" + quote.getQuote());
                    } else {
                        respond(event, "Could not retrieve quote!");
                    }
                    break;
                case ("between"):
                    if (parts.length < 4) {
                        respond(event, ".quote between requires both a lower and an upper bound");
                        return;
                    }
                    Integer lower = null;
                    Integer upper = null;
                    try {
                        lower = Integer.parseInt(parts[2]);
                        upper = Integer.parseInt(parts[3]);
                    } catch (NumberFormatException nfe) {
                        respond(event, ".quote between requires two numbers between 0 and the total number of quotes");
                    }
                    if (lower < 0 || upper > minder.getNumQuotes(channel)) {
                        respond(event, ".quote between requires two numbers between 0 and the total number of quotes");
                    }
                    quote = minder.getQuoteBetween(channel, false, lower, upper);
                    if (quote != null) {
                        respond(event, "#" + quote.getQuoteID() + " | Submitted: " + quote.getAuthor() + " | Date: " + quote.getTimestamp().toString());
                        respond(event, "" + quote.getQuote());
                    } else {
                        respond(event, "Could not retrieve quote!");
                    }
                    break;
                default:
                    Integer num = null;
                    try {
                        num = Integer.parseInt(parts[1]);
                    } catch (NumberFormatException nfe) {
                    }
                    if (num == null) {
                        respond(event, String.format("Unknown quote command %s", parts[1]));
                        return;
                    }
                    try {
                        if (num < 0) {
                            respond(event, String.format("Quote numbers are always positive integers, %d is invalid", num));
                            return;
                        }
                        quote = minder.getQuote(num, channel, server);
                        if (quote != null) {
                            respond(event, "#" + quote.getQuoteID() + " | Submitted: " + quote.getAuthor() + " | Date: " + quote.getTimestamp().toString());
                            respond(event, "" + quote.getQuote());
                        } else {
                            respond(event, "Could not retrieve quote!");
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
            }
        } else {
            quote = minder.getQuote(channel, server, false);
            if (quote != null) {
                respond(event, "#" + quote.getQuoteID() + " | Submitted: " + quote.getAuthor() + " | Date: " + quote.getTimestamp().toString());
                respond(event, "" + quote.getQuote());
            } else {
                respond(event, "Could not retrieve quote!");
            }
        }
    }

    //#################### Utility quote response ######################
    public void respond(MessageEvent event, String response) {
        event.respondChannel("[Q] " + response);
    }
}
