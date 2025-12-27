const CACHE_NAME = 'juko-cherry-intake-v1';
const OFFLINE_URL = '/offline.html'; // Optional fallback page

const urlsToCache = [
  '/',
  '/clerk/cherry-intake',
  '/css/bootstrap.min.css',
  '/js/bootstrap.bundle.min.js',
  // Add any other static files you use
];

// Install event – cache static files
self.addEventListener('install', event => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(cache => cache.addAll(urlsToCache))
      .then(() => self.skipWaiting())
  );
});

// Activate event – clean old caches
self.addEventListener('activate', event => {
  event.waitUntil(
    caches.keys().then(cacheNames => {
      return Promise.all(
        cacheNames.filter(name => name !== CACHE_NAME)
          .map(name => caches.delete(name))
      );
    }).then(() => self.clients.claim())
  );
});

// Fetch event – serve from cache first, fallback to network
self.addEventListener('fetch', event => {
  if (event.request.method !== 'GET') {
    event.respondWith(fetch(event.request));
    return;
  }

  event.respondWith(
    caches.match(event.request)
      .then(cachedResponse => {
        // Return cached version if available
        if (cachedResponse) return cachedResponse;

        // Otherwise try network
        return fetch(event.request)
          .then(networkResponse => {
            // Cache successful responses
            if (!networkResponse || networkResponse.status !== 200 || networkResponse.type !== 'basic') {
              return networkResponse;
            }

            const responseToCache = networkResponse.clone();
            caches.open(CACHE_NAME)
              .then(cache => cache.put(event.request, responseToCache));

            return networkResponse;
          })
          .catch(() => {
            // Offline fallback
            return caches.match(OFFLINE_URL);
          });
      })
  );
});