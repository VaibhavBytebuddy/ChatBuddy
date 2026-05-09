---
trigger: always_on
---

# Backend Agent Rules

This directory contains comprehensive rules and guidelines for the backend codebase, organized by topic for easy reference and maintenance.

## Quick Reference

| File | Description | Key Topics |
|------|-------------|------------|
| [01-architecture](./rules/01-architecture.md) | Project structure & patterns | Stack, packages, layers, data flows |
| [02-coding-style](./rules/02-coding-style.md) | Code style & conventions | Naming, Lombok, imports, null handling |
| [03-code-structure](./rules/03-code-structure.md) | Component templates | Controllers, Services, DTOs, Mappers |
| [04-testing](./rules/04-testing.md) | Test patterns & data | Integration tests, seed data, assertions |
| [05-kafka-exceptions](./rules/05-kafka-exceptions.md) | Event-driven & errors | Publishers, Listeners, ErrorCodes |
| [06-naming-conventions](./rules/06-naming-conventions.md) | Naming patterns | Components, constants, variables |
| [07-agent-actions](./rules/07-agent-actions.md) | Permissions & guardrails | Allowed/restricted actions |
| [08-execution-guidelines](./rules/08-execution-guidelines.md) | Feature creation steps | 15-step feature guide, checklist |
| [09-critical-constraints](./rules/09-critical-constraints.md) | Must-follow rules | DI, null safety, code quality |

## Core Principles

1. **No inline comments** - Code is self-documenting
2. **Fully qualified names** - `customerOrderEntity`, not `entity`
3. **Constructor injection** - Use `@RequiredArgsConstructor`
4. **Null safety** - Use `Optional`, never return `null`
5. **Method brevity** - ≤5 lines ideal, 10 max

## Getting Started

For new features, follow [08-execution-guidelines](./rules/08-execution-guidelines.md) step-by-step.
