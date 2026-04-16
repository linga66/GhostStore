# 🗄️ Distributed Java Cache (Redis-Lite)

> A high-performance, distributed key-value store built from scratch in Java — featuring custom LRU Eviction, TTL-based expiration, and Consistent Hashing for horizontal scaling.

![Build](https://img.shields.io/badge/Build-Maven-blue)
![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.2-green)

---

## 📌 Overview

Most caching systems are just simple HashMaps. This project implements the **core internals** of systems like Redis and Memcached, focusing on memory management and distributed systems logic.

Rather than using off-the-shelf libraries, every key primitive — eviction, expiry, and request routing — is hand-rolled to deeply understand the underlying mechanics.

---

## ✨ Key Features

| Feature | Description |
|---|---|
| **Custom LRU Cache** | Built with `ConcurrentHashMap` + a custom Doubly Linked List. O(1) access and eviction. |
| **TTL Expiration** | Per-entry time-to-live support. Stale data is automatically invalidated. |
| **Consistent Hashing** | MD5-based Hash Ring distributes data evenly, minimizing re-sharding when nodes join/leave. |
| **Cluster Proxy Pattern** | Any node acts as a smart proxy — wrong-node requests are transparently routed to the correct owner via REST. |

---

## 🏗️ Architecture & Design

### 1. The Cache Engine (Local Node)

Each node maintains its own LRU cache backed by two structures:

- **`ConcurrentHashMap`** — thread-safe, O(1) key lookup
- **Doubly Linked List** — tracks recency of access. When capacity is reached, the tail (least recently used node) is evicted in O(1) time.

### 2. The Distributed Layer (Cluster Mode)

Data distribution is handled by a **Consistent Hashing Ring** spanning 2⁶⁴ slots.

- Each physical node is assigned **multiple virtual nodes** on the ring for even load distribution.
- A key's hash position determines which node "owns" it — the first node encountered clockwise on the ring.
- Adding or removing a node only re-maps a small fraction of keys (unlike simple modulo hashing).

### 3. Request Flow

```
Client → GET /user123 → Node A
            │
            ▼
     Hash("user123") → Check Ring
            │
     ┌──────┴──────┐
     │             │
  Node A owns    Node B owns
  → serve local  → proxy to Node B → return to client
```

---

## 🛠️ Tech Stack

- **Language:** Java 17
- **Framework:** Spring Boot 3.x
- **Node Communication:** Spring `RestTemplate`
- **Build Tool:** Maven

---

## 🚥 Getting Started

### Prerequisites

- JDK 17 or higher
- Maven (or use the included `mvnw` wrapper)

### Running the Cluster

Open **two terminal windows** to simulate a 2-node cluster:

**Node 1 — Port 8081**
```powershell
.\mvnw spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081 --cluster.nodes=localhost:8081,localhost:8082"
```

**Node 2 — Port 8082**
```powershell
.\mvnw spring-boot:run "-Dspring-boot.run.arguments=--server.port=8082 --cluster.nodes=localhost:8081,localhost:8082"
```

---

## 📡 API Reference

### Store a Value
```
POST /api/cache?key={key}&value={value}&ttl={seconds}
```
```bash
curl -X POST "http://localhost:8081/api/cache?key=Sreeja&value=Engineer&ttl=100"
```

### Retrieve a Value
```
GET /api/cache/{key}
```
```bash
curl http://localhost:8082/api/cache/Sreeja
```

> 💡 **Note:** The GET above hits Node 8082, but the key may be owned by Node 8081. The proxy routing handles this transparently — you'll see `"Proxying request to node..."` in the logs.

---

## 📸 Proof of Work

### 1. Cluster Proxying
> Send a GET to Node 8082 for a key stored in Node 8081. The terminal log should show the proxy routing in action.

---

### 2. LRU Eviction
> Fill the cache to capacity and verify the oldest key is evicted (returns `null`).

---

### 3. TTL Expiry
> A GET returns data immediately, then returns `null` after the TTL window elapses.

---

## 🧠 Lessons Learned

**Concurrency Challenges**
Managing thread safety during eviction required careful use of `synchronized` blocks alongside `ConcurrentHashMap` to prevent race conditions.

**Hashing Theory**
Simple modulo hashing breaks when nodes are added or removed — nearly all keys need re-mapping. Consistent Hashing reduces this to only the keys owned by the affected segment of the ring.

**Distributed Orchestration**
Building a cluster where nodes are peer-aware and can route requests without a centralized coordinator was the most challenging and rewarding part of this project.

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).