<header>
    <nav class="bg-brand" role="navigation" aria-label="Hauptnavigation">
        <div class="flex flex-wrap items-center justify-between px-4 py-2">
            <a href="/" class="flex items-center">
                <img src="/images/ri-logo.png" class="max-h-7" alt="Ranking-Info Logo"
                     onerror="this.style.display='none'">
            </a>
            <button type="button" id="nav-burger" class="md:hidden text-white p-2 rounded hover:bg-white/20"
                    aria-label="Menü öffnen" aria-expanded="false" aria-controls="nav-menu">
                <span class="block w-5 h-0.5 bg-white mb-1"></span>
                <span class="block w-5 h-0.5 bg-white mb-1"></span>
                <span class="block w-5 h-0.5 bg-white"></span>
            </button>
            <div id="nav-menu" class="hidden w-full md:flex md:w-auto md:items-center md:gap-1 pb-2 md:pb-0">
                <a href="/players" class="text-white hover:bg-white/20 px-3 py-2 rounded text-sm block md:inline-block">Spieler</a>
                <a href="/listings" class="text-white hover:bg-white/20 px-3 py-2 rounded text-sm block md:inline-block">Listen</a>
                <a href="/clubs" class="text-white hover:bg-white/20 px-3 py-2 rounded text-sm block md:inline-block">Vereine</a>
                <a href="/federations" class="text-white hover:bg-white/20 px-3 py-2 rounded text-sm block md:inline-block">Verbände</a>
                <a href="/status" class="text-white hover:bg-white/20 px-3 py-2 rounded text-sm block md:inline-block">Status</a>
                <a href="/help" class="text-white hover:bg-white/20 px-3 py-2 rounded text-sm block md:inline-block">Hilfe</a>
                <a href="/privacy" class="text-white hover:bg-white/20 px-3 py-2 rounded text-sm block md:inline-block">Datenschutz</a>
                <a href="/about" class="text-white hover:bg-white/20 px-3 py-2 rounded text-sm block md:inline-block">Über</a>
            </div>
        </div>
    </nav>
    <script>
        const burger = document.getElementById('nav-burger');
        const menu = document.getElementById('nav-menu');
        burger?.addEventListener('click', () => {
            const isHidden = menu.classList.toggle('hidden');
            burger.setAttribute('aria-expanded', isHidden ? 'false' : 'true');
        });
    </script>
</header>
