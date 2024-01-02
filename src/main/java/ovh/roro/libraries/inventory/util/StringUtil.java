package ovh.roro.libraries.inventory.util;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class StringUtil {

    public static @NotNull String @NotNull [] splitNewline(@NotNull String input) {
        Preconditions.checkNotNull(input);

        List<String> list = new ObjectArrayList<>();

        int index = input.indexOf('\n');
        int lastIndex = 0;
        while (index != -1) {
            list.add(input.substring(lastIndex, index));

            lastIndex = index + 1;
            index = input.indexOf('\n', index + 1);
        }

        if (lastIndex != input.length()) {
            list.add(input.substring(lastIndex));
        }

        return list.toArray(String[]::new);
    }
}
