param(
    [string]$ElasticsearchUrl = "http://localhost:9200"
)

$ErrorActionPreference = "Stop"

$baseUrl = $ElasticsearchUrl.TrimEnd("/")
$profileIndex = "customer-profiles"
$eventIndex = "customer-events"

function Invoke-ElasticJson {
    param(
        [Parameter(Mandatory = $true)][string]$Method,
        [Parameter(Mandatory = $true)][string]$Path,
        [object]$Body = $null,
        [string]$ContentType = "application/json"
    )

    $uri = "$baseUrl$Path"
    if ($null -eq $Body) {
        return Invoke-RestMethod -Method $Method -Uri $uri
    }

    $payload = if ($Body -is [string]) { $Body } else { $Body | ConvertTo-Json -Depth 30 }
    return Invoke-RestMethod -Method $Method -Uri $uri -ContentType $ContentType -Body $payload
}

function Ensure-Index {
    param(
        [Parameter(Mandatory = $true)][string]$IndexName,
        [Parameter(Mandatory = $true)][object]$Mapping
    )

    try {
        Invoke-ElasticJson -Method Head -Path "/$IndexName" | Out-Null
    } catch {
        if ($_.Exception.Response.StatusCode.value__ -ne 404) {
            throw
        }
        Invoke-ElasticJson -Method Put -Path "/$IndexName" -Body $Mapping | Out-Null
    }
}

$profileMapping = @{
    mappings = @{
        properties = @{
            profileKey = @{ type = "keyword" }
            anonymousId = @{ type = "keyword" }
            email = @{ type = "keyword" }
            phoneNumber = @{ type = "keyword" }
            identifiers = @{ type = "object"; enabled = $true }
            properties = @{ type = "object"; enabled = $true }
            segmentIds = @{ type = "keyword" }
            segmentKeys = @{ type = "keyword" }
            createdAt = @{ type = "date" }
            updatedAt = @{ type = "date" }
        }
    }
}

$eventMapping = @{
    mappings = @{
        properties = @{
            profileId = @{ type = "keyword" }
            eventType = @{ type = "keyword" }
            source = @{ type = "keyword" }
            payload = @{ type = "object"; enabled = $true }
            occurredAt = @{ type = "date" }
            receivedAt = @{ type = "date" }
        }
    }
}

Ensure-Index -IndexName $profileIndex -Mapping $profileMapping
Ensure-Index -IndexName $eventIndex -Mapping $eventMapping

$profiles = @(
    @{
        id = "sample-profile-ada"
        document = @{
            profileKey = "email:ada@example.com"
            anonymousId = "anon-ada-001"
            email = "ada@example.com"
            phoneNumber = "+84901234567"
            identifiers = @{
                email = "ada@example.com"
                phoneNumber = "+84901234567"
                custom = @{
                    loyaltyId = "LOY-ADA-001"
                }
            }
            properties = @{
                age = 31
                language = "vi"
                favoriteColor = @("green", "black")
                firstName = "Ada"
                lastName = "Nguyen"
                birthDate = "1995-04-12T00:00:00.000+00:00"
                lifetimeValue = 1850.75
                lastSeenAt = "2026-06-29T10:15:00.000+00:00"
                interestTags = @("electronics", "loyalty", "premium")
                preferredCategories = @("laptop", "audio")
                scoreHistory = @(82, 91, 95)
            }
            segmentIds = @(
                "38b66935-b497-4d88-8745-c41c7ab1bc1e",
                "e170a1fa-573c-43dc-9f74-b548a49697bc",
                "693b972a-e133-42b0-8c64-3b1df7e2e573"
            )
            segmentKeys = @("adultCustomers", "vipCustomers", "vietnameseCustomers")
            createdAt = "2026-06-20T08:00:00.000+00:00"
            updatedAt = "2026-06-29T10:15:00.000+00:00"
        }
    },
    @{
        id = "sample-profile-ben"
        document = @{
            profileKey = "email:ben@example.com"
            anonymousId = "anon-ben-002"
            email = "ben@example.com"
            phoneNumber = "+14155550101"
            identifiers = @{
                email = "ben@example.com"
                phoneNumber = "+14155550101"
                custom = @{
                    loyaltyId = "LOY-BEN-002"
                }
            }
            properties = @{
                age = 24
                language = "en"
                favoriteColor = @("blue")
                firstName = "Ben"
                lastName = "Carter"
                birthDate = "2002-02-02T00:00:00.000+00:00"
                lifetimeValue = 420.5
                lastSeenAt = "2026-06-28T14:30:00.000+00:00"
                interestTags = @("sports", "shoes")
                preferredCategories = @("fashion", "sportswear")
                scoreHistory = @(55, 63, 68)
            }
            segmentIds = @("38b66935-b497-4d88-8745-c41c7ab1bc1e")
            segmentKeys = @("adultCustomers")
            createdAt = "2026-06-21T09:00:00.000+00:00"
            updatedAt = "2026-06-28T14:30:00.000+00:00"
        }
    },
    @{
        id = "sample-profile-chi"
        document = @{
            profileKey = "email:chi@example.com"
            anonymousId = "anon-chi-003"
            email = "chi@example.com"
            phoneNumber = "+84987654321"
            identifiers = @{
                email = "chi@example.com"
                phoneNumber = "+84987654321"
                custom = @{
                    loyaltyId = "LOY-CHI-003"
                }
            }
            properties = @{
                age = 17
                language = "vi"
                favoriteColor = @("white", "pink")
                firstName = "Chi"
                lastName = "Tran"
                birthDate = "2009-08-18T00:00:00.000+00:00"
                lifetimeValue = 92
                lastSeenAt = "2026-06-27T08:45:00.000+00:00"
                interestTags = @("books", "school")
                preferredCategories = @("books")
                scoreHistory = @(35, 40, 42)
            }
            segmentIds = @("693b972a-e133-42b0-8c64-3b1df7e2e573")
            segmentKeys = @("vietnameseCustomers")
            createdAt = "2026-06-22T11:00:00.000+00:00"
            updatedAt = "2026-06-27T08:45:00.000+00:00"
        }
    },
    @{
        id = "sample-profile-dana"
        document = @{
            profileKey = "email:dana@example.com"
            anonymousId = "anon-dana-004"
            email = "dana@example.com"
            phoneNumber = "+442071234567"
            identifiers = @{
                email = "dana@example.com"
                phoneNumber = "+442071234567"
                custom = @{
                    loyaltyId = "LOY-DANA-004"
                }
            }
            properties = @{
                age = 42
                language = "en"
                favoriteColor = @("red", "gold")
                firstName = "Dana"
                lastName = "Miller"
                birthDate = "1984-12-05T00:00:00.000+00:00"
                lifetimeValue = 2740.2
                lastSeenAt = "2026-06-30T00:10:00.000+00:00"
                interestTags = @("travel", "premium", "credit-card")
                preferredCategories = @("travel", "luxury")
                scoreHistory = @(88, 90, 97)
            }
            segmentIds = @(
                "38b66935-b497-4d88-8745-c41c7ab1bc1e",
                "e170a1fa-573c-43dc-9f74-b548a49697bc"
            )
            segmentKeys = @("adultCustomers", "vipCustomers")
            createdAt = "2026-06-19T06:00:00.000+00:00"
            updatedAt = "2026-06-30T00:10:00.000+00:00"
        }
    }
)

$events = @(
    @{
        id = "sample-event-ada-purchase-001"
        document = @{
            profileId = "sample-profile-ada"
            eventType = "purchase"
            source = "web"
            payload = @{
                eventGroupId = "ORDER-ADA-001"
                productId = "LAPTOP-13-PRO"
                currency = "USD"
                quantity = 1
                unitSalePrice = 1299.99
                campaignId = "summer-vip"
                sourceMedium = "email"
                discountCodes = @("VIP10")
                occurredAtClient = "2026-06-29T10:12:00.000+00:00"
            }
            occurredAt = "2026-06-29T10:12:00.000+00:00"
            receivedAt = "2026-06-29T10:12:03.000+00:00"
        }
    },
    @{
        id = "sample-event-ada-pageview-001"
        document = @{
            profileId = "sample-profile-ada"
            eventType = "pageView"
            source = "web"
            payload = @{
                url = "/products/laptop-13-pro"
                campaignId = "summer-vip"
                sourceMedium = "email"
                occurredAtClient = "2026-06-29T10:05:00.000+00:00"
            }
            occurredAt = "2026-06-29T10:05:00.000+00:00"
            receivedAt = "2026-06-29T10:05:01.000+00:00"
        }
    },
    @{
        id = "sample-event-ben-cart-001"
        document = @{
            profileId = "sample-profile-ben"
            eventType = "cartAbandoned"
            source = "web"
            payload = @{
                eventGroupId = "CART-BEN-001"
                productId = "RUNNING-SHOE-42"
                currency = "USD"
                quantity = 2
                unitSalePrice = 79.5
                cartValue = 159
                campaignId = "sports-week"
                sourceMedium = "paid-search"
                occurredAtClient = "2026-06-28T14:20:00.000+00:00"
            }
            occurredAt = "2026-06-28T14:20:00.000+00:00"
            receivedAt = "2026-06-28T14:20:04.000+00:00"
        }
    },
    @{
        id = "sample-event-chi-pageview-001"
        document = @{
            profileId = "sample-profile-chi"
            eventType = "pageView"
            source = "web"
            payload = @{
                url = "/books/exam-prep"
                campaignId = "back-to-school"
                sourceMedium = "organic"
                occurredAtClient = "2026-06-27T08:44:00.000+00:00"
            }
            occurredAt = "2026-06-27T08:44:00.000+00:00"
            receivedAt = "2026-06-27T08:44:02.000+00:00"
        }
    },
    @{
        id = "sample-event-dana-purchase-001"
        document = @{
            profileId = "sample-profile-dana"
            eventType = "purchase"
            source = "mobile"
            payload = @{
                eventGroupId = "ORDER-DANA-001"
                productId = "TRAVEL-PACKAGE-SEA"
                currency = "USD"
                quantity = 1
                unitSalePrice = 920
                campaignId = "luxury-travel"
                sourceMedium = "app-push"
                discountCodes = @("TRAVEL50")
                occurredAtClient = "2026-06-30T00:08:00.000+00:00"
            }
            occurredAt = "2026-06-30T00:08:00.000+00:00"
            receivedAt = "2026-06-30T00:08:05.000+00:00"
        }
    },
    @{
        id = "sample-event-dana-pageview-001"
        document = @{
            profileId = "sample-profile-dana"
            eventType = "pageView"
            source = "mobile"
            payload = @{
                url = "/travel/sea-package"
                campaignId = "luxury-travel"
                sourceMedium = "app-push"
                occurredAtClient = "2026-06-29T23:55:00.000+00:00"
            }
            occurredAt = "2026-06-29T23:55:00.000+00:00"
            receivedAt = "2026-06-29T23:55:01.000+00:00"
        }
    }
)

$bulkLines = New-Object System.Collections.Generic.List[string]
foreach ($profile in $profiles) {
    $bulkLines.Add((@{ index = @{ _index = $profileIndex; _id = $profile.id } } | ConvertTo-Json -Compress -Depth 10))
    $bulkLines.Add(($profile.document | ConvertTo-Json -Compress -Depth 30))
}
foreach ($event in $events) {
    $bulkLines.Add((@{ index = @{ _index = $eventIndex; _id = $event.id } } | ConvertTo-Json -Compress -Depth 10))
    $bulkLines.Add(($event.document | ConvertTo-Json -Compress -Depth 30))
}

$bulkPayload = ($bulkLines -join "`n") + "`n"
$bulkResponse = Invoke-ElasticJson -Method Post -Path "/_bulk?refresh=true" -Body $bulkPayload -ContentType "application/x-ndjson"

if ($bulkResponse.errors) {
    $bulkResponse.items | ConvertTo-Json -Depth 20
    throw "Elasticsearch bulk seed completed with errors."
}

$profileCount = (Invoke-ElasticJson -Method Get -Path "/$profileIndex/_count").count
$eventCount = (Invoke-ElasticJson -Method Get -Path "/$eventIndex/_count").count

Write-Host "Seeded Elasticsearch sample data successfully."
Write-Host "Profiles indexed: $($profiles.Count), total in index: $profileCount"
Write-Host "Events indexed: $($events.Count), total in index: $eventCount"

