@import java.util.List
@import java.util.Map
@import de.hdawg.rankinginfo.service.QuarterEntry
@import de.hdawg.rankinginfo.web.RankingRow
@param Map<String, List<QuarterEntry>> quarters = null
@param List<String> federations = null
@param Map<String, Object> searchParams = null
@param List<RankingRow> rankings = null
@param String danger = null
@template.layout(title = "Ranglisten | Ranking-Info", danger = danger, content = @`
<main>
    <h1 class="text-2xl font-bold text-left mb-3 mt-3">Ranglisten</h1>
    <form action="/listings" method="get" class="mb-4"
          hx-get="/listings" hx-target="#results" hx-indicator="#results-loading" hx-push-url="true">
        <div class="flex flex-wrap items-start gap-3">
            <div>
                <label for="quarter" class="block text-xs text-gray-500">Stichtag</label>
                <select id="quarter" name="quarter" class="border border-gray-300 rounded px-2 py-1">
                    @if(quarters != null)
                    @for(var entry : quarters.entrySet())
                    <optgroup label="${entry.getKey()}">
                        @for(var opt : entry.getValue())
                        <option value="${opt.second()}" selected="${searchParams != null && opt.second().equals(searchParams.get("quarter"))}">${opt.first()}</option>
                        @endfor
                    </optgroup>
                    @endfor
                    @endif
                </select>
                <label for="year_end" class="flex items-center gap-1 text-xs text-gray-500 mt-1">
                    <input type="checkbox" id="year_end" name="year_end" value="1"
                           checked="${searchParams != null && "1".equals(searchParams.get("year_end"))}"> Abschlussrangliste
                </label>
            </div>
            <div>
                <label for="gender" class="block text-xs text-gray-500">Geschlecht</label>
                <select id="gender" name="gender" class="border border-gray-300 rounded px-2 py-1">
                    @for(var g : java.util.List.of("Herren", "Damen", "Junioren", "Juniorinnen"))
                    <option value="${g}" selected="${searchParams != null && g.equals(searchParams.get("gender"))}">${g}</option>
                    @endfor
                </select>
            </div>
            <div>
                <label for="age_group" class="block text-xs text-gray-500">Altersklasse</label>
                <select id="age_group" name="age_group" class="border border-gray-300 rounded px-2 py-1">
                    <option value=""></option>
                    @for(var ag : java.util.List.of("U11", "U12", "U13", "U14", "U15", "U16", "U17", "U18"))
                    <option value="${ag}" selected="${searchParams != null && ag.equals(searchParams.get("age_group"))}">${ag}</option>
                    @endfor
                </select>
            </div>
            <div>
                <label for="age_group_options" class="block text-xs text-gray-500">Extras</label>
                <select id="age_group_options" name="age_group_options" class="border border-gray-300 rounded px-2 py-1">
                    <option value=""></option>
                    <option value="only_yob" selected="${searchParams != null && "only_yob".equals(searchParams.get("age_group_options"))}">nur Geburtsjahrgang</option>
                    <option value="include_younger" selected="${searchParams != null && "include_younger".equals(searchParams.get("age_group_options"))}">auch Jüngere einbeziehen</option>
                </select>
            </div>
            <div>
                <label for="federation" class="block text-xs text-gray-500">Verband</label>
                <select id="federation" name="federation" class="border border-gray-300 rounded px-2 py-1">
                    <option value=""></option>
                    @if(federations != null)
                    @for(var f : federations)
                    <option value="${f}" selected="${searchParams != null && f.equals(searchParams.get("federation"))}">${f}</option>
                    @endfor
                    @endif
                </select>
            </div>
            <div>
                <label for="club" class="block text-xs text-gray-500">Verein</label>
                <input type="search" id="club" name="club" class="border border-gray-300 rounded px-2 py-1"
                       size="10" placeholder="Verein" value="${searchParams != null && searchParams.get("club") != null ? searchParams.get("club").toString() : ""}">
            </div>
            <div>
                <div class="block text-xs invisible">.</div>
                <button type="submit" name="commit" value="1"
                        class="bg-brand text-white px-3 py-1 text-sm rounded hover:opacity-90 cursor-pointer">
                    Liste abrufen
                </button>
            </div>
        </div>
    </form>

    <div id="results-loading" class="htmx-indicator text-gray-500 text-sm mt-2">Laden…</div>

    @template.listing.results(rankings = rankings)
</main>
`)
