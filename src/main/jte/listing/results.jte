@import java.util.List
@import de.hdawg.rankinginfo.web.RankingRow
@param List<RankingRow> rankings = null
<div id="results">
@if(rankings == null)
    <div class="rounded shadow mt-3">
        <header class="bg-gray-100 px-4 py-3 rounded-t font-semibold border-b border-gray-200">
            <span>Ranglisten - Suchen und Filtern, auch in historischen Daten</span>
        </header>
        <div class="p-4">
            Hier bekommst Du direkten Zugriff auf die Aktiven- und Jugendranglisten - nicht nur aus dem aktuellen Jahr,
            sondern auch historische Daten.
            <br/><br/>
            Aktuelle Optionen:
            <ul class="list-disc list-inside mt-2">
                <li><b>Quartal</b> - das anzuzeigende Quartal zum Stichtag</li>
                <li><b>Abschlussrangliste</b> - ist ein Abschlussquartal angewählt, wird die Abschließende Liste für das Jahr angezeigt, sonst die für das Folgequartal gültige (neue Altersklassen)</li>
                <li><b>Geschlecht</b></li>
                <li><b>Altersklasse*</b> - die anzuzeigende Altersklasse. Bei ungeraden Altersklassen
                    erhält man die Liste nur für diesen Jahrgang (außer es sollen auch Jüngere angezeigt werden)</li>
                <li><b>Optionen*</b> - Anzeige nur von dem in diesem Quartal gültigen Jahrgangs oder Einbeziehen auch der sonst
                    nicht berücksichtigten jüngeren Geburtsjahrgänge</li>
                <li><b>Verband</b> - Nur Einträge von Spielerinnen und Spielern aus dem ausgewählten Verband</li>
                <li><b>Verein</b> - Einträge anzeigen, die den eingegebenen Text im Vereinsnamen enthalten</li>
            </ul>
            <small>* hat nur für die Jugendranglisten eine Auswirkung</small>
        </div>
        <footer class="flex border-t border-gray-200 rounded-b">
            <a class="flex-1 text-center px-4 py-3 text-brand hover:bg-gray-50" href="/players">Spielersuche</a>
            <a class="flex-1 text-center px-4 py-3 text-brand hover:bg-gray-50 border-l border-gray-200" href="/clubs">Vereinssuche</a>
            <a class="flex-1 text-center px-4 py-3 text-brand hover:bg-gray-50 border-l border-gray-200" href="/federations">Verbandsstatistik</a>
        </footer>
    </div>
@elseif(!rankings.isEmpty())
    <div class="overflow-x-auto mb-2">
        <table class="w-full">
            <thead>
            <tr class="bg-gray-100">
                <th scope="col" class="py-2 px-2 hidden md:table-cell">#</th>
                <th scope="col" class="py-2 px-2">Rang</th>
                <th scope="col" class="py-2 px-2">Nachname, Vorname</th>
                <th scope="col" class="py-2 px-2">DTB-ID</th>
                <th scope="col" class="py-2 px-2 hidden md:table-cell">Nationalität</th>
                <th scope="col" class="py-2 px-2 hidden md:table-cell">Verein</th>
                <th scope="col" class="py-2 px-2 hidden md:table-cell">Verband</th>
                <th scope="col" class="py-2 px-2">Punkte</th>
            </tr>
            </thead>
            <tbody>
            @for(var r : rankings)
            <tr class="border-b border-gray-200 even:bg-gray-50">
                <td class="text-center py-1 px-2 hidden md:table-cell">${r.rowNumber()}</td>
                <td class="py-1 px-2">
                    <span>${r.rankingPosition()}</span>
                    @if("up".equals(r.positionChange()))
                    <span class="ri-icon-green" style="margin-left: 5px">
                        @template.fragments.icons.arrow_up()
                    </span>
                    @elseif("down".equals(r.positionChange()))
                    <span class="ri-icon-red" style="margin-left: 5px">
                        @template.fragments.icons.arrow_down()
                    </span>
                    @endif
                </td>
                <td class="py-1 px-2">${r.displayName()}</td>
                <td class="py-1 px-2">
                    <a href="${r.playerUrl()}" class="text-brand">${r.dtbId()}</a>
                </td>
                <td class="text-center py-1 px-2 hidden md:table-cell">
                    <img src="${r.flagSrc()}" width="30" alt="${r.nationality()}">
                </td>
                <td class="py-1 px-2 hidden md:table-cell">
                    <a href="${r.clubUrl()}" class="text-brand">${r.club()}</a>
                </td>
                <td class="text-center py-1 px-2 hidden md:table-cell">${r.federation()}</td>
                <td class="py-1 px-2">${r.score()}</td>
            </tr>
            @endfor
            </tbody>
        </table>
    </div>
@else
    <div class="mt-4 text-gray-600">
        Es wurden leider keine Daten für diese Suchkriterien gefunden.
    </div>
@endif
</div>
