@param String imprintName = ""
@param String imprintStreet = ""
@param String imprintZipcode = ""
@param String imprintCity = ""
@param String imprintPhone = ""
@param String imprintMail = ""
@param String danger = null
@template.layout(title = "Über | Ranking-Info", danger = danger, content = @`
    <section class="ri-hero mt-5 mb-5">
        <p class="ri-hero-tag">Über das Projekt</p>
        <h1>Ranking-Info</h1>
        <p class="max-w-2xl">Eine Webanwendung, die die Aktiven- und Jugendrangliste des Deutschen Tennis Bundes
            auf verschiedene Arten visualisiert — mit Zugriff auf historische Daten.</p>
    </section>

    <div class="grid grid-cols-1 md:grid-cols-2 gap-4 pb-8">
        <div class="ri-card ri-card-pad">
            <h2 class="flex items-center gap-2 text-base font-semibold mb-2">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 448 512" fill="currentColor" class="ri-icon text-accent" style="width:1.15em;height:1.15em" aria-hidden="true"><path d="M448 80l0 48c0 44.2-100.3 80-224 80S0 172.2 0 128L0 80C0 35.8 100.3 0 224 0S448 35.8 448 80zM393.2 214.7c20.8-7.4 39.9-16.9 54.8-28.6L448 288c0 44.2-100.3 80-224 80S0 332.2 0 288L0 186.1c14.9 11.8 34 21.2 54.8 28.6C99.7 230.7 159.5 240 224 240s124.3-9.3 169.2-25.3zM0 346.1c14.9 11.8 34 21.2 54.8 28.6C99.7 390.7 159.5 400 224 400s124.3-9.3 169.2-25.3c20.8-7.4 39.9-16.9 54.8-28.6l0 85.9c0 44.2-100.3 80-224 80S0 476.2 0 432l0-85.9z"/></svg>
                Datenquelle
            </h2>
            <p class="text-sm text-muted leading-relaxed">
                Alle Daten stammen aus der offiziellen DTB-Rangliste. Sie werden manuell auf Basis der jeweils aktuellen
                Fassung eingepflegt und um historische Stände ergänzt.
            </p>
            <a href="https://www.tennis.de/spielen/ranglisten-und-leistungsklasse.html" target="_blank"
               class="inline-flex items-center gap-1.5 text-sm text-brand-light mt-3">
                Offizielle Rangliste auf tennis.de
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" fill="currentColor" style="width:0.85em;height:0.85em" aria-hidden="true"><path d="M320 0c-17.7 0-32 14.3-32 32s14.3 32 32 32l50.7 0L201.4 233.4c-12.5 12.5-12.5 32.8 0 45.3s32.8 12.5 45.3 0L416 109.3l0 50.7c0 17.7 14.3 32 32 32s32-14.3 32-32l0-128c0-17.7-14.3-32-32-32L320 0zM80 32C35.8 32 0 67.8 0 112L0 432c0 44.2 35.8 80 80 80l320 0c44.2 0 80-35.8 80-80l0-112c0-17.7-14.3-32-32-32s-32 14.3-32 32l0 112c0 8.8-7.2 16-16 16L80 448c-8.8 0-16-7.2-16-16l0-320c0-8.8 7.2-16 16-16l112 0c17.7 0 32-14.3 32-32s-14.3-32-32-32L80 32z"/></svg>
            </a>
        </div>

        <div class="ri-card ri-card-pad">
            <h2 class="flex items-center gap-2 text-base font-semibold mb-2">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 576 512" fill="currentColor" class="ri-icon text-accent" style="width:1.15em;height:1.15em" aria-hidden="true"><path d="M0 96C0 60.7 28.7 32 64 32l448 0c35.3 0 64 28.7 64 64l0 320c0 35.3-28.7 64-64 64L64 480c-35.3 0-64-28.7-64-64L0 96zM64 96l0 320 448 0 0-320L64 96zm80 64a48 48 0 1 1 0 96 48 48 0 1 1 0-96zm-16 144l32 0c30.9 0 56 25.1 56 56c0 8.8-7.2 16-16 16L88 376c-8.8 0-16-7.2-16-16c0-30.9 25.1-56 56-56zm240-72c0-8.8 7.2-16 16-16l112 0c8.8 0 16 7.2 16 16s-7.2 16-16 16l-112 0c-8.8 0-16-7.2-16-16zm0 64c0-8.8 7.2-16 16-16l112 0c8.8 0 16 7.2 16 16s-7.2 16-16 16l-112 0c-8.8 0-16-7.2-16-16z"/></svg>
                Impressum
            </h2>
            <div class="ri-imprint">
                <div class="row">
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 448 512" fill="currentColor" class="ri-icon" aria-hidden="true"><path d="M224 256A128 128 0 1 0 224 0a128 128 0 1 0 0 256zm-45.7 48C79.8 304 0 383.8 0 482.3C0 498.7 13.3 512 29.7 512l388.6 0c16.4 0 29.7-13.3 29.7-29.7C448 383.8 368.2 304 269.7 304l-91.4 0z"/></svg>
                    <span class="lbl">Name</span><span class="val">${imprintName}</span>
                </div>
                <div class="row">
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 384 512" fill="currentColor" class="ri-icon" aria-hidden="true"><path d="M215.7 499.2C267 435 384 279.4 384 192C384 86 298 0 192 0S0 86 0 192c0 87.4 117 243 168.3 307.2c12.3 15.3 35.1 15.3 47.4 0zM192 128a64 64 0 1 1 0 128 64 64 0 1 1 0-128z"/></svg>
                    <span class="lbl">Adresse</span><span class="val">${imprintStreet}, ${imprintZipcode} ${imprintCity}</span>
                </div>
                <div class="row">
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" fill="currentColor" class="ri-icon" aria-hidden="true"><path d="M164.9 24.6c-7.7-18.6-28-28.5-47.4-23.2l-88 24C12.1 30.2 0 46 0 64C0 311.4 200.6 512 448 512c18 0 33.8-12.1 38.6-29.5l24-88c5.3-19.4-4.6-39.7-23.2-47.4l-96-40c-16.3-6.8-35.2-2.1-46.3 11.6L304.7 368C234.3 334.7 177.3 277.7 144 207.3L193.3 167c13.7-11.2 18.4-30 11.6-46.3l-40-96z"/></svg>
                    <span class="lbl">Telefon</span><span class="val"><a href="tel:${imprintPhone}">${imprintPhone}</a></span>
                </div>
                <div class="row">
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" fill="currentColor" class="ri-icon" aria-hidden="true"><path d="M48 64C21.5 64 0 85.5 0 112c0 15.1 7.1 29.3 19.2 38.4L236.8 313.6c11.4 8.5 27 8.5 38.4 0L492.8 150.4c12.1-9.1 19.2-23.3 19.2-38.4c0-26.5-21.5-48-48-48L48 64zM0 176L0 384c0 35.3 28.7 64 64 64l384 0c35.3 0 64-28.7 64-64l0-208L294.4 339.2c-22.8 17.1-54 17.1-76.8 0L0 176z"/></svg>
                    <span class="lbl">E-Mail</span><span class="val"><a href="mailto:${imprintMail}">${imprintMail}</a></span>
                </div>
            </div>
        </div>
    </div>

    <div class="pb-8">
        @template.fragments.supportProject()
    </div>
`)
