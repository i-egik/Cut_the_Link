import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
    import java.time.LocalDateTime;

public class main{
    public static void main(String[] args) {


        class ShortenedLink {
            private String originalUrl;
            private String shortUrl;
            private int maxRedirects;
            private int currentRedirects;
            private LocalDateTime expirationTime;
            private UUID userId;

            public ShortenedLink(String originalUrl, String shortUrl, int maxRedirects, LocalDateTime expirationTime, UUID userId) {
                this.originalUrl = originalUrl;
                this.shortUrl = shortUrl;
                this.maxRedirects = maxRedirects;
                this.currentRedirects = 0;
                this.expirationTime = expirationTime;
                this.userId = userId;
            }

            public boolean isExpired() {
                return LocalDateTime.now().isAfter(expirationTime);
            }

            public boolean isRedirectLimitReached() {
                return currentRedirects >= maxRedirects;
            }

            public void incrementRedirects() {
                this.currentRedirects++;
            }

            public String getOriginalUrl() {
                return originalUrl;
            }

            public String getShortUrl() {
                return shortUrl;
            }

            public int getMaxRedirects() {
                return maxRedirects;
            }

            public int getCurrentRedirects() {
                return currentRedirects;
            }

            public UUID getUserId() {
                return userId;
            }
        }

        class LinkShorteningService {
            private Map<String, ShortenedLink> shortenedLinks = new HashMap<>();
            private Map<UUID, String> userUUIDs = new HashMap<>();

            public String shortenUrl(UUID userId, String originalUrl, int maxRedirects, long ttlInMinutes) {
                String shortUrl = generateShortUrl();
                LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(ttlInMinutes);

                ShortenedLink shortenedLink = new ShortenedLink(originalUrl, shortUrl, maxRedirects, expirationTime, userId);
                shortenedLinks.put(shortUrl, shortenedLink);

                // Сохраняем UUID пользователя, если это первый запрос
                if (!userUUIDs.containsKey(userId)) {
                    userUUIDs.put(userId, shortUrl);
                }

                return shortUrl;
            }

            public String redirect(String shortUrl) {
                ShortenedLink shortenedLink = shortenedLinks.get(shortUrl);

                if (shortenedLink == null) {
                    return "Ссылка не найдена.";
                }

                if (shortenedLink.isExpired()) {
                    return "Срок жизни ссылки истек.";
                }

                if (shortenedLink.isRedirectLimitReached()) {
                    return "Лимит переходов по ссылке исчерпан.";
                }

                // Увеличиваем количество переходов
                shortenedLink.incrementRedirects();
                return shortenedLink.getOriginalUrl();
            }

            private String generateShortUrl() {
                // Генерируем случайный короткий URL.
                return "new_randomLink" + UUID.randomUUID().toString().substring(0, 6);
            }
        }
                Scanner scanner = new Scanner(System.in);
                LinkShorteningService linkService = new LinkShorteningService();

                System.out.println("Введите ваш UUID (или оставьте пустым для создания нового):");
                String userInput = scanner.nextLine().trim();

                UUID userId;
                if (userInput.isEmpty()) {
                    userId = UUID.randomUUID();
                    System.out.println("Ваш новый UUID: " + userId);
                } else {
                    userId = UUID.fromString(userInput);
                }

                System.out.println("Введите URL для сокращения:");
                String originalUrl = scanner.nextLine();

                System.out.println("Введите максимальное количество переходов:");
                int maxRedirects = Integer.parseInt(scanner.nextLine());

                System.out.println("Введите время жизни ссылки в минутах:");
                long ttlInMinutes = Long.parseLong(scanner.nextLine());

                // Создание короткой ссылки
                String shortUrl = linkService.shortenUrl(userId, originalUrl, maxRedirects, ttlInMinutes);
                System.out.println("Ваша короткая ссылка: " + shortUrl);


                while (true) {
                    System.out.println("\nВведите короткую ссылку для редиректа или 'exit' для выхода:");
                    String redirectInput = scanner.nextLine().trim();
                    if ("exit".equalsIgnoreCase(redirectInput)) {
                        try {
                            Desktop.getDesktop().browse(new URI(originalUrl));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } catch (URISyntaxException e) {
                            throw new RuntimeException(e);
                        }
                        ;
                    }

                    String result = linkService.redirect(redirectInput);
                    System.out.println(result);
                }

    }
        }
