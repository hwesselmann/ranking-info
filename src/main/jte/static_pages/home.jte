@param String start = ""
@param String danger = null
@template.layout(title = "Ranking-Info", danger = danger, content = @`
    <section class="ri-hero mt-4 mb-6">
        <p class="ri-hero-tag">Tennis · Ranglisten · Analyse</p>
        <h1>Willkommen bei Ranking-Info</h1>
        <p class="max-w-2xl">Der Anlaufpunkt zur Deutschen Tennis-Rangliste — filterbar, historisch
            und immer auf dem aktuellen Stand.</p>
        <div class="flex flex-wrap gap-3 mt-5">
            <a href="/listings" class="ri-btn ri-btn-accent">Ranglisten ansehen</a>
            <a href="/players" class="ri-btn ri-btn-ghost">Spieler suchen</a>
        </div>
    </section>

    <div class="ri-card mb-6">
        <div class="ri-card-header">Ranglisteninformationen</div>
        <div class="ri-card-pad text-sm leading-relaxed text-ink">
            Hier findest Du eine filterbare Version der Deutschen Aktiven- und Jugendranglisten.
            Die Daten werden manuell auf Basis der offiziellen Listen vom Deutschen Tennis Bund eingepflegt.
            <br><br>
            Der Vorteil zu den offiziellen Listen? Es ist nicht nur das aktuelle Quartal und die Liste zum
            Jahresabschluss verfügbar — alle importierten Daten sind seit dem <span class="font-medium">${start}</span> enthalten.
            <br><br>
            Viel Spaß beim Stöbern!
        </div>
        <div class="ri-card-footer">
            <a href="/players">Spielersuche</a>
            <a href="/listings">Ranglisten</a>
            <a href="/clubs">Vereinssuche</a>
            <a href="/federations">Verbandsstatistik</a>
        </div>
    </div>
`)
