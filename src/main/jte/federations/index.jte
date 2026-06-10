@import java.util.Map
@param Map<String, Map<String, Integer>> federations = null
@param String danger = null
@template.layout(title = "Verbandsstatistik | Ranking-Info", danger = danger, content = @`
    <h1 class="text-2xl font-semibold mt-5 mb-1">Daten zu den Landesverbänden</h1>
    <p class="text-sm text-muted mb-5 max-w-2xl">Wie viele Spielerinnen und Spieler der DTB-Landesverbände nach Altersklasse
        in der aktuellen Periode auf der Rangliste geführt sind.</p>
    <div class="ri-table-wrap mb-4">
        <table class="ri-table" role="table" aria-label="Verbandsstatistik">
            <thead>
            <tr>
                <th scope="col" rowspan="2">Verband</th>
                <th scope="col" class="center" colspan="2" style="border-left:1px solid rgba(255,255,255,0.15)">U12</th>
                <th scope="col" class="center" colspan="2" style="border-left:1px solid rgba(255,255,255,0.15)">U14</th>
                <th scope="col" class="center" colspan="2" style="border-left:1px solid rgba(255,255,255,0.15)">U16</th>
                <th scope="col" class="center" colspan="2" style="border-left:1px solid rgba(255,255,255,0.15)">U18</th>
                <th scope="col" class="center" colspan="2" style="border-left:1px solid rgba(255,255,255,0.15)">Aktive</th>
            </tr>
            <tr>
                <th scope="col" class="center" style="border-left:1px solid rgba(255,255,255,0.15)">@template.fragments.icons.mars()<span class="sr-only">männlich</span></th>
                <th scope="col" class="center">@template.fragments.icons.venus()<span class="sr-only">weiblich</span></th>
                <th scope="col" class="center" style="border-left:1px solid rgba(255,255,255,0.15)">@template.fragments.icons.mars()<span class="sr-only">männlich</span></th>
                <th scope="col" class="center">@template.fragments.icons.venus()<span class="sr-only">weiblich</span></th>
                <th scope="col" class="center" style="border-left:1px solid rgba(255,255,255,0.15)">@template.fragments.icons.mars()<span class="sr-only">männlich</span></th>
                <th scope="col" class="center">@template.fragments.icons.venus()<span class="sr-only">weiblich</span></th>
                <th scope="col" class="center" style="border-left:1px solid rgba(255,255,255,0.15)">@template.fragments.icons.mars()<span class="sr-only">männlich</span></th>
                <th scope="col" class="center">@template.fragments.icons.venus()<span class="sr-only">weiblich</span></th>
                <th scope="col" class="center" style="border-left:1px solid rgba(255,255,255,0.15)">@template.fragments.icons.mars()<span class="sr-only">männlich</span></th>
                <th scope="col" class="center">@template.fragments.icons.venus()<span class="sr-only">weiblich</span></th>
            </tr>
            </thead>
            <tbody>
            @if(federations != null)
            @for(var entry : federations.entrySet())
            <tr>
                <td class="font-medium">${entry.getKey()}</td>
                <td class="center" style="border-left:1px solid rgba(11,58,117,0.07)">${entry.getValue().containsKey("U12m") ? String.valueOf(entry.getValue().get("U12m")) : "–"}</td>
                <td class="center">${entry.getValue().containsKey("U12w") ? String.valueOf(entry.getValue().get("U12w")) : "–"}</td>
                <td class="center" style="border-left:1px solid rgba(11,58,117,0.07)">${entry.getValue().containsKey("U14m") ? String.valueOf(entry.getValue().get("U14m")) : "–"}</td>
                <td class="center">${entry.getValue().containsKey("U14w") ? String.valueOf(entry.getValue().get("U14w")) : "–"}</td>
                <td class="center" style="border-left:1px solid rgba(11,58,117,0.07)">${entry.getValue().containsKey("U16m") ? String.valueOf(entry.getValue().get("U16m")) : "–"}</td>
                <td class="center">${entry.getValue().containsKey("U16w") ? String.valueOf(entry.getValue().get("U16w")) : "–"}</td>
                <td class="center" style="border-left:1px solid rgba(11,58,117,0.07)">${entry.getValue().containsKey("U18m") ? String.valueOf(entry.getValue().get("U18m")) : "–"}</td>
                <td class="center">${entry.getValue().containsKey("U18w") ? String.valueOf(entry.getValue().get("U18w")) : "–"}</td>
                <td class="center" style="border-left:1px solid rgba(11,58,117,0.07)">${entry.getValue().containsKey("m00") ? String.valueOf(entry.getValue().get("m00")) : "–"}</td>
                <td class="center">${entry.getValue().containsKey("w00") ? String.valueOf(entry.getValue().get("w00")) : "–"}</td>
            </tr>
            @endfor
            @endif
            </tbody>
        </table>
    </div>
`)
