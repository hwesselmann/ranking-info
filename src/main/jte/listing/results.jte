@import java.util.List
@import de.hdawg.rankinginfo.web.RankingRow
@param List<RankingRow> rankings = null
<div id="results">
@if(rankings == null)
    <div class="ri-card mt-3">
        <div class="ri-card-header">Ranglisten – Suchen und Filtern, auch in historischen Daten</div>
        <div class="ri-card-pad text-sm leading-relaxed text-ink">
            Hier bekommst Du direkten Zugriff auf die Aktiven- und Jugendranglisten – nicht nur aus dem aktuellen Jahr,
            sondern auch historische Daten.
            <p class="font-medium text-ink mt-3 mb-1">Aktuelle Optionen:</p>
            <ul class="list-disc list-inside space-y-1 text-muted">
                <li><span class="font-medium text-ink">Quartal</span> – das anzuzeigende Quartal zum Stichtag</li>
                <li><span class="font-medium text-ink">Abschlussrangliste</span> – bei einem Abschlussquartal die abschließende Liste für das Jahr, sonst die für das Folgequartal gültige (neue Altersklassen)</li>
                <li><span class="font-medium text-ink">Geschlecht</span></li>
                <li><span class="font-medium text-ink">Altersklasse*</span> – bei ungeraden Altersklassen nur dieser Jahrgang (außer es sollen auch Jüngere angezeigt werden)</li>
                <li><span class="font-medium text-ink">Optionen*</span> – nur der in diesem Quartal gültige Jahrgang oder auch jüngere Geburtsjahrgänge</li>
                <li><span class="font-medium text-ink">Verband</span> – nur Einträge aus dem ausgewählten Verband</li>
                <li><span class="font-medium text-ink">Verein</span> – Einträge, die den Text im Vereinsnamen enthalten</li>
            </ul>
            <p class="text-xs text-faint mt-2">* hat nur für die Jugendranglisten eine Auswirkung</p>
        </div>
        <div class="ri-card-footer">
            <a href="/players">Spielersuche</a>
            <a href="/clubs">Vereinssuche</a>
            <a href="/federations">Verbandsstatistik</a>
        </div>
    </div>
@elseif(!rankings.isEmpty())
    <div class="ri-table-wrap mb-2">
        <table class="ri-table">
            <thead>
            <tr>
                <th scope="col" class="hidden md:table-cell">#</th>
                <th scope="col">Rang</th>
                <th scope="col">Nachname, Vorname</th>
                <th scope="col">DTB-ID</th>
                <th scope="col" class="hidden md:table-cell">Nationalität</th>
                <th scope="col" class="hidden md:table-cell">Verein</th>
                <th scope="col" class="hidden md:table-cell">Verband</th>
                <th scope="col">Punkte</th>
            </tr>
            </thead>
            <tbody>
            @for(var r : rankings)
            <tr>
                <td class="center hidden md:table-cell text-muted">${r.rowNumber()}</td>
                <td>
                    <span class="font-medium">${r.rankingPosition()}</span>
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
                <td>${r.displayName()}</td>
                <td>
                    <a href="${r.playerUrl()}" class="text-brand-light">${r.dtbId()}</a>
                </td>
                <td class="center hidden md:table-cell">
                    <img src="${r.flagSrc()}" width="30" alt="${r.nationality()}">
                </td>
                <td class="hidden md:table-cell">
                    <a href="${r.clubUrl()}" class="text-brand-light">${r.club()}</a>
                </td>
                <td class="center hidden md:table-cell">${r.federation()}</td>
                <td class="font-medium">${r.score()}</td>
            </tr>
            @endfor
            </tbody>
        </table>
    </div>
@else
    <div class="ri-note mt-4">
        Es wurden leider keine Daten für diese Suchkriterien gefunden.
    </div>
@endif
</div>
