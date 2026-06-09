@import java.util.Map
@param Map<String, Map<String, Integer>> federations = null
@param String danger = null
@template.layout(title = "Verbandsstatistik | Ranking-Info", danger = danger, content = @`
    <h1 class="text-2xl font-bold text-left mb-3 mt-3 tracking-normal">Daten zu den Landesverbänden</h1>
    <p class="mb-3">Hier finden Sie Daten, wie viele Spielerinnen und Spieler der DTB Landesverbände nach Altersklasse in der aktuellen Periode auf der Rangliste geführt sind.</p>
    <div class="overflow-x-auto mt-3 mb-3">
        <table class="w-full" role="table" aria-label="Verbandsstatistik">
            <thead>
            <tr class="bg-gray-800 text-white font-bold">
                <th scope="col" class="py-2 px-2 text-left">Verband</th>
                <th scope="col" class="py-2 px-2 text-center border-l border-gray-600" colspan="2">U12</th>
                <th scope="col" class="py-2 px-2 text-center border-l border-gray-600" colspan="2">U14</th>
                <th scope="col" class="py-2 px-2 text-center border-l border-gray-600" colspan="2">U16</th>
                <th scope="col" class="py-2 px-2 text-center border-l border-gray-600" colspan="2">U18</th>
                <th scope="col" class="py-2 px-2 text-center border-l border-gray-600" colspan="2">Aktive</th>
            </tr>
            <tr class="bg-gray-100 text-gray-900">
                <th scope="col" class="py-1 px-2"></th>
                <th scope="col" class="py-2 px-2 text-center border-l border-gray-300">@template.fragments.icons.mars()<span class="sr-only">männlich</span></th>
                <th scope="col" class="py-2 px-2 text-center">@template.fragments.icons.venus()<span class="sr-only">weiblich</span></th>
                <th scope="col" class="py-2 px-2 text-center border-l border-gray-300">@template.fragments.icons.mars()<span class="sr-only">männlich</span></th>
                <th scope="col" class="py-2 px-2 text-center">@template.fragments.icons.venus()<span class="sr-only">weiblich</span></th>
                <th scope="col" class="py-2 px-2 text-center border-l border-gray-300">@template.fragments.icons.mars()<span class="sr-only">männlich</span></th>
                <th scope="col" class="py-2 px-2 text-center">@template.fragments.icons.venus()<span class="sr-only">weiblich</span></th>
                <th scope="col" class="py-2 px-2 text-center border-l border-gray-300">@template.fragments.icons.mars()<span class="sr-only">männlich</span></th>
                <th scope="col" class="py-2 px-2 text-center">@template.fragments.icons.venus()<span class="sr-only">weiblich</span></th>
                <th scope="col" class="py-2 px-2 text-center border-l border-gray-300">@template.fragments.icons.mars()<span class="sr-only">männlich</span></th>
                <th scope="col" class="py-2 px-2 text-center">@template.fragments.icons.venus()<span class="sr-only">weiblich</span></th>
            </tr>
            </thead>
            <tbody>
            @if(federations != null)
            @for(var entry : federations.entrySet())
            <tr class="border-b border-gray-200 even:bg-gray-50">
                <td class="py-1 px-2">${entry.getKey()}</td>
                <td class="py-1 px-2 text-center border-l border-gray-300">${entry.getValue().containsKey("U12m") ? String.valueOf(entry.getValue().get("U12m")) : "-"}</td>
                <td class="py-1 px-2 text-center">${entry.getValue().containsKey("U12w") ? String.valueOf(entry.getValue().get("U12w")) : "-"}</td>
                <td class="py-1 px-2 text-center border-l border-gray-300">${entry.getValue().containsKey("U14m") ? String.valueOf(entry.getValue().get("U14m")) : "-"}</td>
                <td class="py-1 px-2 text-center">${entry.getValue().containsKey("U14w") ? String.valueOf(entry.getValue().get("U14w")) : "-"}</td>
                <td class="py-1 px-2 text-center border-l border-gray-300">${entry.getValue().containsKey("U16m") ? String.valueOf(entry.getValue().get("U16m")) : "-"}</td>
                <td class="py-1 px-2 text-center">${entry.getValue().containsKey("U16w") ? String.valueOf(entry.getValue().get("U16w")) : "-"}</td>
                <td class="py-1 px-2 text-center border-l border-gray-300">${entry.getValue().containsKey("U18m") ? String.valueOf(entry.getValue().get("U18m")) : "-"}</td>
                <td class="py-1 px-2 text-center">${entry.getValue().containsKey("U18w") ? String.valueOf(entry.getValue().get("U18w")) : "-"}</td>
                <td class="py-1 px-2 text-center border-l border-gray-300">${entry.getValue().containsKey("m00") ? String.valueOf(entry.getValue().get("m00")) : "-"}</td>
                <td class="py-1 px-2 text-center">${entry.getValue().containsKey("w00") ? String.valueOf(entry.getValue().get("w00")) : "-"}</td>
            </tr>
            @endfor
            @endif
            </tbody>
        </table>
    </div>
`)
