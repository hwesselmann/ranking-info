@import java.util.List
@import java.util.Map
@import de.hdawg.rankinginfo.domain.Ranking
@param List<Ranking> players = null
@param Map<String, Object> searchParams = null
@param String danger = null
@template.layout(title = "Spielersuche | Ranking-Info", danger = danger, content = @`
    <h1 class="text-2xl font-semibold mt-5 mb-4">Spielersuche</h1>
    <form action="/players" method="get" class="mb-4"
          hx-get="/players" hx-target="#results" hx-indicator="#results-loading" hx-push-url="true">
        <div class="flex flex-wrap items-center gap-2">
            <input type="search" name="dtb_id" class="ri-input"
                   placeholder="DTB-ID" value="${searchParams != null && searchParams.get("dtb_id") != null ? searchParams.get("dtb_id").toString() : ""}">
            <input type="search" name="lastname" class="ri-input"
                   placeholder="Nachname" value="${searchParams != null && searchParams.get("lastname") != null ? searchParams.get("lastname").toString() : ""}">
            <input type="search" name="yob" class="ri-input"
                   placeholder="Geburtsjahr" value="${searchParams != null && searchParams.get("yob") != null ? searchParams.get("yob").toString() : ""}">
            <button type="submit" name="commit" value="1" class="ri-btn ri-btn-primary">suchen</button>
        </div>
    </form>

    <div id="results-loading" class="htmx-indicator text-muted text-sm mt-2">Laden…</div>

    @template.players.results(players = players)
`)
