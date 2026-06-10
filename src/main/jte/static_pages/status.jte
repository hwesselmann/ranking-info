@import java.util.Map
@import java.util.Set
@param String dateLastUpdated = ""
@param long playerFemaleCount = 0
@param long playerMaleCount = 0
@param long rankingCount = 0
@param Map<Integer, Set<Integer>> quartersByYear = null
@param String danger = null
@template.layout(title = "Status | Ranking-Info", danger = danger, content = @`
    <h1 class="text-2xl font-semibold mt-5 mb-3">Status &amp; Datenbestand</h1>

    <div class="ri-status-badge mb-6">
        <span class="k">letztes Update</span>
        <span class="v">${dateLastUpdated}</span>
    </div>

    <div class="grid grid-cols-1 sm:grid-cols-3 gap-3 mb-7">
        <div class="ri-metric">
            <p class="label">Spielerinnen</p>
            <p class="value">${playerFemaleCount}</p>
        </div>
        <div class="ri-metric">
            <p class="label">Spieler</p>
            <p class="value">${playerMaleCount}</p>
        </div>
        <div class="ri-metric">
            <p class="label">Einzelrankings</p>
            <p class="value">${rankingCount}</p>
        </div>
    </div>

    <h2 class="text-base font-semibold mb-1">Verfügbare Quartale</h2>
    <p class="text-xs text-faint italic mb-3">
        * Stichtag 31.12. &nbsp; ** Stichtag 31.03. &nbsp; *** Stichtag 30.06. &nbsp; **** Stichtag 30.09.
    </p>
    <div class="ri-table-wrap mb-4">
        <table class="ri-table" role="table" aria-label="Verfügbare Quartale">
            <thead>
            <tr>
                <th scope="col">Jahr</th>
                <th scope="col" class="center">Q1*</th>
                <th scope="col" class="center">Q2**</th>
                <th scope="col" class="center">Q3***</th>
                <th scope="col" class="center">Q4****</th>
            </tr>
            </thead>
            <tbody>
            @if(quartersByYear != null)
            @for(var entry : quartersByYear.entrySet())
            <tr>
                <td class="font-medium">${entry.getKey()}</td>
                <td class="center">
                    @if(entry.getValue().contains(1))<span class="ri-icon-teal">@template.fragments.icons.check_circle()</span>@else<span class="text-faint">–</span>@endif
                </td>
                <td class="center">
                    @if(entry.getValue().contains(4))<span class="ri-icon-teal">@template.fragments.icons.check_circle()</span>@else<span class="text-faint">–</span>@endif
                </td>
                <td class="center">
                    @if(entry.getValue().contains(7))<span class="ri-icon-teal">@template.fragments.icons.check_circle()</span>@else<span class="text-faint">–</span>@endif
                </td>
                <td class="center">
                    @if(entry.getValue().contains(10))<span class="ri-icon-teal">@template.fragments.icons.check_circle()</span>@else<span class="text-faint">–</span>@endif
                </td>
            </tr>
            @endfor
            @endif
            </tbody>
        </table>
    </div>

    <div class="ri-note">
        In Quartal 3/2020 wurde wegen der COVID-19-Pandemie keine neue Rangliste veröffentlicht.
    </div>
`)
