@import java.util.List
@import java.util.Map
@import de.hdawg.rankinginfo.domain.Ranking
@param List<Ranking> players = null
@param Map<String, Object> searchParams = null
@param String danger = null
@template.layout(title = "Spielersuche | Ranking-Info", danger = danger, content = @`
    <h1 class="text-2xl font-bold text-left mb-3 mt-3">Spielersuche</h1>
    <form action="/players" method="get" class="mb-3"
          hx-get="/players" hx-target="#results" hx-indicator="#results-loading" hx-push-url="true">
        <div class="flex flex-wrap items-center gap-2">
            <input type="search" name="dtb_id" class="border border-gray-300 rounded px-2 py-1 text-sm"
                   placeholder="DTB-ID" value="${searchParams != null && searchParams.get("dtb_id") != null ? searchParams.get("dtb_id").toString() : ""}">
            <input type="search" name="lastname" class="border border-gray-300 rounded px-2 py-1 text-sm"
                   placeholder="Nachname" value="${searchParams != null && searchParams.get("lastname") != null ? searchParams.get("lastname").toString() : ""}">
            <input type="search" name="yob" class="border border-gray-300 rounded px-2 py-1 text-sm"
                   placeholder="Geburtsjahr" value="${searchParams != null && searchParams.get("yob") != null ? searchParams.get("yob").toString() : ""}">
            <button type="submit" name="commit" value="1"
                    class="bg-brand text-white px-3 py-1 text-sm rounded hover:opacity-90 cursor-pointer">suchen</button>
        </div>
    </form>

    <div id="results-loading" class="htmx-indicator text-gray-500 text-sm mt-2">Laden…</div>

    @template.players.results(players = players)
`)
