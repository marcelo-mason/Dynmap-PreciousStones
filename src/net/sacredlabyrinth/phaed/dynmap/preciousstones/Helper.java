package net.sacredlabyrinth.phaed.dynmap.preciousstones;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Helper
{
    /**
     * Converts color codes to <span> with inline css, pipes to <br/>
     *
     * @param msg
     * @return
     */
    public static String colorToHTML(String msg, String sAdditional)
    {
        String out = "";

        msg = msg.trim();
        msg = msg.replace("&", "\u00a7");
        msg = msg.replace("|", "<br/>");
        String[] sections = msg.split("\\u00a7");

        boolean doneFirst = false;
        if(msg.isEmpty()){
            return "";
        }
        boolean hasFirst = msg.substring(0, 1).equals("\u00a7");

        if(!sAdditional.isEmpty()){
            out += "<div style='" + sAdditional + ";'>";
        }

        for (String section : sections)
        {
            if (!section.isEmpty())
            {
                if (!doneFirst && !hasFirst)
                {
                    out += section;
                    doneFirst = true;
                    continue;
                }

                if (section.length() == 1)
                {
                    continue;
                }

                String color = section.substring(0, 1);
                String text = section.substring(1);

                out += "<span style='color:" + colorCodeToHEX(color) + ";'>" + text + "</span>";
            }
        }
        if(!sAdditional.isEmpty()){
            out += "</div>";
        }

        return out;
    }

    private static String colorCodeToHEX(String code)
    {
        if (code.equalsIgnoreCase("0"))
        {
            return "#222";
        }
        if (code.equalsIgnoreCase("1"))
        {
            return "#00A";
        }
        if (code.equalsIgnoreCase("2"))
        {
            return "#0A0";
        }
        if (code.equalsIgnoreCase("3"))
        {
            return "#0AA";
        }
        if (code.equalsIgnoreCase("4"))
        {
            return "#A00";
        }
        if (code.equalsIgnoreCase("5"))
        {
            return "#A0A";
        }
        if (code.equalsIgnoreCase("6"))
        {
            return "#FA0";
        }
        if (code.equalsIgnoreCase("7"))
        {
            return "#AAA";
        }
        if (code.equalsIgnoreCase("8"))
        {
            return "#555";
        }
        if (code.equalsIgnoreCase("9"))
        {
            return "#55F";
        }
        if (code.equalsIgnoreCase("a"))
        {
            return "#5F5";
        }
        if (code.equalsIgnoreCase("b"))
        {
            return "#5FF";
        }
        if (code.equalsIgnoreCase("c"))
        {
            return "#F55";
        }
        if (code.equalsIgnoreCase("d"))
        {
            return "#F5F";
        }
        if (code.equalsIgnoreCase("e"))
        {
            return "#FF5";
        }
        if (code.equalsIgnoreCase("f"))
        {
            return "#FFF";
        }

        return "#FFF";
    }

    /**
     * Returns a prettier coordinate, does not include world
     *
     * @param loc
     * @return
     */
    public static String toLocationString(Location loc)
    {
        return loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ() + " " + loc.getWorld().getName();
    }

    /**
     * Converts string array to ArrayList<String>, remove empty strings
     *
     * @param values
     * @return
     */
    public static List<String> fromArray(String... values)
    {
        List<String> results = new ArrayList<String>();
        Collections.addAll(results, values);
        results.remove("");
        return results;
    }

    /**
     * Converts a player array to ArrayList<Player>
     *
     * @param values
     * @return
     */
    public static List<Player> fromPlayerArray(Player... values)
    {
        List<Player> results = new ArrayList<Player>();
        Collections.addAll(results, values);
        return results;
    }

    /**
     * Converts List<String> to string array
     *
     * @param list
     * @return
     */
    @SuppressWarnings("unchecked")
    public static String[] toArray(List<String> list)
    {
        return list.toArray(new String[0]);
    }

    /**
     * Removes trailing separators
     *
     * @param msg
     * @param sep
     * @return
     */
    public static String stripTrailing(String msg, String sep)
    {
        if (msg.length() < sep.length() * 2)
        {
            return msg;
        }

        String out = msg;
        String first = msg.substring(0, sep.length());
        String last = msg.substring(msg.length() - sep.length(), msg.length());

        if (first.equals(sep))
        {
            out = msg.substring(sep.length());
        }

        if (last.equals(sep))
        {
            out = msg.substring(0, msg.length() - sep.length());
        }

        return out;
    }

    /**
     * Removes first item from a string array
     *
     * @param args
     * @return
     */
    public static String[] removeFirst(String[] args)
    {
        List<String> out = fromArray(args);

        if (!out.isEmpty())
        {
            out.remove(0);
        }
        return toArray(out);
    }

    /**
     * Converts a string array to a space separated string
     *
     * @param args
     * @return
     */
    public static String toMessage(String[] args)
    {
        String out = "";

        for (String arg : args)
        {
            out += arg + " ";
        }

        return out.trim();
    }

    /**
     * Converts a string array to a string with custom separators
     *
     * @param args
     * @param sep
     * @return
     */
    public static String toMessage(String[] args, String sep)
    {
        String out = "";

        for (String arg : args)
        {
            out += arg + sep;
        }

        return stripTrailing(out, sep);
    }

    /**
     * Converts a string array to a string with custom separators
     *
     * @param args
     * @param sep
     * @return
     */
    public static String toMessage(List<String> args, String sep)
    {
        String out = "";

        for (String arg : args)
        {
            out += arg + sep;
        }

        return stripTrailing(out, sep);
    }

}
