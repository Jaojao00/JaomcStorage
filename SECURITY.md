<p align="center">
  <img src="https://img.shields.io/badge/Security-Policy-2D7D46?style=for-the-badge&logo=shield&logoColor=white" alt="Security Policy">
</p>

<h1 align="center">🛡️ Security Policy</h1>
<p align="center"><i>JaomcStorage — Chính sách bảo mật & Hướng dẫn báo cáo lỗ hổng</i></p>

---

## 📋 Supported Versions

| Version | Supported          | Notes                          |
|---------|--------------------|--------------------------------|
| 2.0.x   | ✅ Actively supported | Current stable release         |
| 1.0.x   | ❌ End of life       | Please upgrade to 2.0          |

> **Note:** Only the latest minor release of each major version receives security patches. We strongly recommend running the most recent version at all times.

---

## 🔐 Security Features

JaomcStorage 2.0 implements the following security measures:

- **SQL Injection Prevention** — All database queries use prepared statements with parameterized inputs
- **Permission System** — Granular permission nodes (`jaostorage.kho`, `jaostorage.admin`) via Bukkit/Vault
- **Input Validation** — All command arguments are validated and sanitized before processing
- **Access Control** — Coop storage access is restricted to authorized members only
- **Async Database I/O** — Database operations run on separate threads to prevent exploitation of main thread blocking
- **WAL Mode SQLite** — Write-Ahead Logging prevents data corruption during concurrent access
- **Action Logging** — All sensitive operations (sell, withdraw, admin actions) are logged with timestamps

---

## 🚨 Reporting a Vulnerability

We take security issues seriously. If you discover a vulnerability in JaomcStorage, please follow these steps:

### ⚠️ DO NOT

- ❌ Open a public GitHub issue for security vulnerabilities
- ❌ Post vulnerability details in public forums or Discord
- ❌ Exploit the vulnerability on production servers

### ✅ DO

1. **Email us directly** at: `taidevnguyen.security@gmail.com`
2. **Include the following details:**
   - Description of the vulnerability
   - Steps to reproduce the issue
   - Potential impact assessment
   - Suggested fix (if applicable)
   - Your JaomcStorage version and server environment (Spigot/Paper version, Java version)

3. **Use this template:**

```
Subject: [SECURITY] JaomcStorage - Brief Description

## Vulnerability Report

**Version affected:** 2.0.x
**Server software:** Paper 1.21.x
**Java version:** 17

**Description:**
[Detailed description of the vulnerability]

**Steps to Reproduce:**
1. ...
2. ...
3. ...

**Expected behavior:**
[What should happen]

**Actual behavior:**
[What actually happens — the security issue]

**Impact:**
[Low / Medium / High / Critical]

**Suggested fix:**
[Optional — your recommendation]
```

---

## ⏱️ Response Timeline

| Stage                  | Timeframe          |
|------------------------|--------------------|
| Acknowledgment         | Within **48 hours**  |
| Initial assessment     | Within **5 days**    |
| Patch development      | Within **14 days**   |
| Security advisory      | Upon patch release   |

---

## 🏗️ Build Provenance & Supply Chain Security

This project uses **build provenance attestation** to ensure the integrity and authenticity of all release artifacts.

### GitHub Actions — Artifact Attestation

Every build produced through our CI/CD pipeline is attested using [`actions/attest-build-provenance`](https://github.com/actions/attest-build-provenance), which generates a signed [SLSA](https://slsa.dev/) provenance statement for the build artifact.

```yaml
- name: Tài Dev Nguyễn
  uses: actions/attest-build-provenance@v4.1.0
```

**What this provides:**

- ✅ **Tamper-proof builds** — Cryptographic proof that artifacts were built from this repository
- ✅ **SLSA Provenance** — Verifiable metadata about the build process (source, builder, parameters)
- ✅ **Supply chain protection** — Assurance that released JARs have not been modified after build
- ✅ **GitHub Attestations API** — Attestations are stored and queryable via GitHub's API

### Verifying Build Provenance

You can verify the provenance of any release artifact using the GitHub CLI:

```bash
gh attestation verify jaostorage-2.0.jar --repo <OWNER>/JaomcStorage
```

---

## 🔒 Best Practices for Server Administrators

To keep your server secure when using JaomcStorage:

1. **Keep the plugin updated** — Always run the latest version
2. **Restrict admin permissions** — Only grant `jaostorage.admin` to trusted operators
3. **Review action logs** — Periodically check logs for suspicious activity
4. **Backup your database** — Regularly backup `storage.db` in case of corruption
5. **Use a firewall** — Ensure your server is behind proper network protection
6. **Validate configs** — After editing `config.yml`, verify no sensitive data is exposed
7. **Monitor coop access** — Audit coop member lists to prevent unauthorized storage access

---

## 📜 Disclosure Policy

- We follow a **coordinated disclosure** model
- Security patches will be released before public disclosure
- Credit will be given to reporters (unless anonymity is requested)
- A security advisory will be published on GitHub after the fix is released

---

## 🏆 Acknowledgments

We appreciate the security research community. Responsible disclosure helps keep the Minecraft server ecosystem safe for everyone.

If you have contributed to the security of JaomcStorage, your name will be listed here (with your permission):

| Contributor | Date | Description |
|-------------|------|-------------|
| — | — | *Be the first to help!* |

---

<p align="center">
  <b>🛡️ JaomcStorage Security</b> — Maintained by <b>Tài Nguyễn</b>
  <br>
  <i>Keeping your Skyblock server safe & secure</i>
</p>
