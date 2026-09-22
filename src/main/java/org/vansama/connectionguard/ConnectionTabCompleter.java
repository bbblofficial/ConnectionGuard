package org.vansama.connectionguard;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class ConnectionTabCompleter implements TabCompleter {

    private final Connection connection;

    public ConnectionTabCompleter(Connection connection) {
        this.connection = connection;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command,
                                      String alias, String[] args) {
        List<String> out = new ArrayList<String>();

        if (args.length == 1) {
            String[] subs = {"help","creator","ping","on","off","toggle","status",
                    "bypass","unbypass","list","forceping","clearping","set","reload"};
            String prefix = args[0].toLowerCase();
            for (String s : subs) {
                if (s.startsWith(prefix)) out.add(s);
            }
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("ping") || sub.equals("bypass") || sub.equals("unbypass")
                    || sub.equals("forceping") || sub.equals("clearping")) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        out.add(p.getName());
                    }
                }
            } else if (sub.equals("set")) {
                out.add("ping");
                out.add("grace");
                out.add("interval");
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("set")) {
                out.add("<value>");
            }
        }
        return out;
    }
}
