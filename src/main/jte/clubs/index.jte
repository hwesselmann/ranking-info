@import java.util.List
@import java.util.Map
@import de.hdawg.rankinginfo.web.viewmodel.ClubSummary
@param List<ClubSummary> clubs = null
@param Map<String, Object> searchParams = null
@param String danger = null
@template.layout(title = "Vereinssuche | Ranking-Info", danger = danger, content = @`
    <h1 class="text-2xl font-semibold mt-5 mb-4">Vereinssuche</h1>
    <form action="/clubs" method="get" class="mb-4"
          hx-get="/clubs" hx-target="#results" hx-indicator="#results-loading" hx-push-url="true">
        <div class="flex flex-wrap items-center gap-2">
            <input type="search" name="club" class="ri-input"
                   placeholder="Vereinsname" value="${searchParams != null && searchParams.get("club") != null ? searchParams.get("club").toString() : ""}">
            <button type="submit" name="commit" value="1" class="ri-btn ri-btn-primary">suchen</button>
        </div>
    </form>

    <div id="results-loading" class="htmx-indicator text-muted text-sm mt-2">Laden…</div>

    @template.clubs.results(clubs = clubs)
`)
