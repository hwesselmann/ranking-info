@import java.util.List
@import de.hdawg.rankinginfo.domain.Ranking
@param List<Ranking> players = null
<div id="results">
@if(players == null)
    <div class="ri-card mt-3">
        <div class="ri-card-header">Spielersuche</div>
        <div class="ri-card-pad text-sm leading-relaxed text-ink">
            <p class="font-medium text-ink mb-1">Der direkte Weg zum Spielerprofil</p>
            Über die Spielersuche kann gezielt nach einer Spielerin oder einem Spieler gesucht werden.
            Wenn Du keine Suchkriterien angibst und auf „suchen" klickst, erhältst Du alle Spieler im System –
            diese Liste kann allerdings ziemlich lang sein.
            <br><br>
            Zur Verfügung stehen die Suche nach <span class="font-medium text-ink">DTB-ID</span>,
            <span class="font-medium text-ink">Nachname (oder Teilen davon)</span> oder
            <span class="font-medium text-ink">Geburtsjahrgang</span>. Bei einer DTB-ID wird nur nach dieser gesucht;
            Nachname und Geburtsjahr sind kombinierbar.
        </div>
        <div class="ri-card-footer">
            <a href="/listings">Ranglisten</a>
            <a href="/clubs">Vereinssuche</a>
            <a href="/federations">Verbandsstatistik</a>
        </div>
    </div>
@elseif(!players.isEmpty())
    <div class="ri-table-wrap">
        <table class="ri-table" role="table" aria-label="Spielerliste">
            <thead>
            <tr>
                <th scope="col">Nachname, Vorname</th>
                <th scope="col" class="center">DTB-ID</th>
                <th scope="col" class="hidden md:table-cell">Verein</th>
            </tr>
            </thead>
            <tbody>
            @for(var p : players)
            <tr>
                <td>
                    <a href="/players/${p.dtbId()}" class="text-brand-light">${p.lastname()}, ${p.firstname()}</a>
                </td>
                <td class="center">${p.dtbId()}</td>
                <td class="hidden md:table-cell">${p.club()}</td>
            </tr>
            @endfor
            </tbody>
        </table>
    </div>
@else
    <div class="ri-note mt-4">Es wurden keine Spieler gefunden.</div>
@endif
</div>
