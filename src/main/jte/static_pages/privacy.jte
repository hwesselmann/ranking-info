@param String danger = null
@template.layout(title = "Datenschutz | Ranking-Info", danger = danger, content = @`
    <h1 class="text-2xl font-semibold mt-5 mb-1">Datenschutz</h1>
    <p class="text-sm text-muted leading-relaxed mb-6 max-w-2xl">Datenschutzerklärung gemäß DSGVO.</p>

    <div class="grid grid-cols-1 md:grid-cols-[170px_minmax(0,1fr)] gap-6 md:gap-8 pb-8">
        <nav class="ri-toc self-start md:sticky md:top-4" aria-label="Abschnitte">
            <p class="ri-toc-title">Abschnitte</p>
            <a href="#ds-1">1 · Auf einen Blick</a>
            <a href="#ds-2">2 · Hosting &amp; CDN</a>
            <a href="#ds-3">3 · Plugins &amp; Tools</a>
        </nav>

        <div class="flex flex-col gap-4">
            <section id="ds-1" class="ri-card ri-card-pad scroll-mt-4">
                <h2 class="flex items-center gap-2.5 text-base font-semibold mb-3">
                    <span class="inline-flex items-center justify-center w-7 h-7 rounded-full bg-surface text-brand text-sm font-semibold">1</span>
                    Datenschutz auf einen Blick
                </h2>
                <p class="text-sm text-muted leading-relaxed mb-3">
                    <span class="font-medium text-ink">Allgemeine Hinweise.</span> Die folgenden Hinweise geben einen
                    einfachen Überblick darüber, was mit Ihren personenbezogenen Daten passiert, wenn Sie diese Website
                    besuchen. Personenbezogene Daten sind alle Daten, mit denen Sie persönlich identifiziert werden können.
                </p>
                <p class="text-sm text-muted leading-relaxed mb-3">
                    <span class="font-medium text-ink">Wer ist verantwortlich?</span> Die Datenverarbeitung erfolgt durch
                    den Websitebetreiber, dessen Kontaktdaten Sie dem Impressum entnehmen können.
                </p>
                <p class="text-sm text-muted leading-relaxed mb-3">
                    <span class="font-medium text-ink">Wie erfassen wir Ihre Daten?</span> Zum einen dadurch, dass Sie uns
                    diese mitteilen (z. B. über ein Kontaktformular). Andere Daten werden automatisch beim Besuch der
                    Website durch unsere IT-Systeme erfasst (vor allem technische Daten wie Browser, Betriebssystem oder
                    Uhrzeit des Seitenaufrufs).
                </p>
                <p class="text-sm text-muted leading-relaxed">
                    <span class="font-medium text-ink">Welche Rechte haben Sie?</span> Sie haben jederzeit das Recht auf
                    unentgeltliche Auskunft über Herkunft, Empfänger und Zweck Ihrer gespeicherten personenbezogenen Daten
                    sowie ein Recht auf Berichtigung oder Löschung. Eine erteilte Einwilligung können Sie jederzeit
                    widerrufen. Außerdem steht Ihnen ein Beschwerderecht bei der zuständigen Aufsichtsbehörde zu.
                </p>
            </section>

            <section id="ds-2" class="ri-card ri-card-pad scroll-mt-4">
                <h2 class="flex items-center gap-2.5 text-base font-semibold mb-3">
                    <span class="inline-flex items-center justify-center w-7 h-7 rounded-full bg-surface text-brand text-sm font-semibold">2</span>
                    Hosting und Content Delivery Networks (CDN)
                </h2>
                <p class="text-sm text-muted leading-relaxed mb-3">
                    <span class="font-medium text-ink">Externes Hosting.</span> Diese Website wird bei einem externen
                    Dienstleister gehostet. Die personenbezogenen Daten werden auf dessen Servern gespeichert — dabei kann
                    es sich u. a. um IP-Adressen, Kontaktanfragen, Meta- und Kommunikationsdaten, Namen und Websitezugriffe
                    handeln.
                </p>
                <p class="text-sm text-muted leading-relaxed mb-3">
                    Der Einsatz des Hosters erfolgt zur Vertragserfüllung gegenüber unseren Kunden (Art. 6 Abs. 1 lit. b
                    DSGVO) und im Interesse einer sicheren, schnellen Bereitstellung unseres Online-Angebots (Art. 6 Abs. 1
                    lit. f DSGVO). Unser Hoster verarbeitet Ihre Daten nur, soweit dies zur Erfüllung seiner
                    Leistungspflichten erforderlich ist.
                </p>
                <div class="ri-note">
                    <span class="font-medium">Eingesetzter Hoster:</span><br>
                    Strato AG · Pascalstraße 10 · 10587 Berlin
                </div>
            </section>

            <section id="ds-3" class="ri-card ri-card-pad scroll-mt-4">
                <h2 class="flex items-center gap-2.5 text-base font-semibold mb-3">
                    <span class="inline-flex items-center justify-center w-7 h-7 rounded-full bg-surface text-brand text-sm font-semibold">3</span>
                    Plugins und Tools
                </h2>
                <p class="text-sm text-muted leading-relaxed mb-3">
                    <span class="font-medium text-ink">Google Web Fonts.</span> Diese Seite nutzt zur einheitlichen
                    Darstellung von Schriftarten sogenannte Web Fonts von Google. Beim Aufruf lädt Ihr Browser die
                    benötigten Schriften in den Cache. Dazu nimmt Ihr Browser Verbindung zu den Servern von Google auf,
                    wodurch Google erfährt, dass über Ihre IP-Adresse diese Website aufgerufen wurde.
                </p>
                <p class="text-sm text-muted leading-relaxed mb-3">
                    Die Nutzung erfolgt auf Grundlage von Art. 6 Abs. 1 lit. f DSGVO (berechtigtes Interesse an
                    einheitlicher Darstellung). Sofern eine Einwilligung abgefragt wurde, erfolgt die Verarbeitung auf
                    Grundlage von Art. 6 Abs. 1 lit. a DSGVO; sie ist jederzeit widerrufbar. Unterstützt Ihr Browser keine
                    Web Fonts, wird eine Standardschrift Ihres Computers genutzt.
                </p>
                <p class="text-sm text-muted leading-relaxed">
                    Weitere Informationen:
                    <a href="https://developers.google.com/fonts/faq" target="_blank" class="text-brand-light">Google Fonts FAQ</a>
                    und die
                    <a href="https://policies.google.com/privacy?hl=de" target="_blank" class="text-brand-light">Datenschutzerklärung von Google</a>.
                </p>
                <p class="text-xs text-faint mt-3">
                    Quelle:
                    <a href="https://www.e-recht24.de/muster-datenschutzerklaerung.html" target="_blank" class="text-brand-light">e-recht24.de</a>
                </p>
            </section>
        </div>
    </div>
`)
