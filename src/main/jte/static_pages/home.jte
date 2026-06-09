@param String start = ""
@param String danger = null
@template.layout(title = "Ranking-Info", danger = danger, content = @`
    <section class="bg-gray-50 py-12">
        <div class="px-4">
            <h1 class="text-3xl font-bold">Willkommen bei Ranking-Info</h1>
            <h2 class="text-xl mt-2 text-gray-600">Der Anlaufpunkt zur Deutschen Tennis-Rangliste</h2>
        </div>
    </section>
    <div class="rounded shadow mt-4">
        <header class="bg-gray-100 px-4 py-3 rounded-t font-semibold border-b border-gray-200">
            Ranglisteninformationen
        </header>
        <div class="p-4">
            Hier findest Du eine filterbare Version der Deutschen Aktiven - und Jugendranglisten.
            Die Daten werden manuell auf Basis der offiziellen Listen vom Deutschen Tennis Bund eingepflegt.<br/>
            Der Vorteil zu den offiziellen Listen? Es ist nicht nur das aktuelle Quartal und die Liste zum
            Jahresabschluss verfügbar, es sind alle importierten Daten seit dem <span>${start}</span> enthalten!
            <br/><br/>
            Viel Spaß beim Stöbern!
        </div>
        <footer class="flex border-t border-gray-200 rounded-b">
            <a href="/players" class="flex-1 text-center px-4 py-3 text-brand hover:bg-gray-50">Spielersuche</a>
            <a href="/listings" class="flex-1 text-center px-4 py-3 text-brand hover:bg-gray-50 border-l border-gray-200">Ranglisten</a>
            <a href="/clubs" class="flex-1 text-center px-4 py-3 text-brand hover:bg-gray-50 border-l border-gray-200">Vereinssuche</a>
            <a href="/federations" class="flex-1 text-center px-4 py-3 text-brand hover:bg-gray-50 border-l border-gray-200">Verbandsstatistik</a>
        </footer>
    </div>
`)
