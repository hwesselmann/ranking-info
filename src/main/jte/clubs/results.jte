@import java.util.List
@import de.hdawg.rankinginfo.web.viewmodel.ClubSummary
@param List<ClubSummary> clubs = null
<div id="results">
@if(clubs == null)
    <div class="ri-card mt-3">
        <div class="ri-card-header">Vereinssuche</div>
        <div class="ri-card-pad text-sm leading-relaxed text-ink">
            Finde alle Vereine, die Spieler auf der aktuellen Aktiven- oder Jugendrangliste haben, sowie die Anzahl
            der aktuellen Ranglistenspieler.
            <br><br>
            Filterbare Ranglisten, Spielerinfos und Datenstände sind ebenfalls abrufbar:
        </div>
        <div class="ri-card-footer">
            <a href="/players">Spielersuche</a>
            <a href="/listings">Ranglisten</a>
            <a href="/federations">Verbandsstatistik</a>
        </div>
    </div>
@elseif(!clubs.isEmpty())
    <div class="ri-table-wrap">
        <table class="ri-table" role="table" aria-label="Vereinsliste">
            <thead>
            <tr>
                <th scope="col">Verein</th>
                <th scope="col" class="center">Jugend</th>
                <th scope="col" class="center">Herren/Damen</th>
            </tr>
            </thead>
            <tbody>
            @for(var club : clubs)
            <tr>
                <td>
                    <a href="/clubs/${club.name()}" class="text-brand-light">${club.name()}</a>
                </td>
                <td class="center">${club.youthCount()}</td>
                <td class="center">${club.adultCount()}</td>
            </tr>
            @endfor
            </tbody>
        </table>
    </div>
@else
    <div class="ri-note mt-4">Es wurden keine Vereine gefunden.</div>
@endif
</div>
