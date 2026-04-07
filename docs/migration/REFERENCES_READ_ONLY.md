# References Directory — Read-Only Policy

**Status:** ACTIVE  
**Date:** 2026-03-28

## Policy

The `references/` directory is **read-only**. No file under `references/` may be created, modified, or deleted as part
of any work on the main Grimoire project.

## Rationale

The reference projects (`grimoire`, `net-bullet`, `ecs-simulation`, `october`) serve as **input** for architectural
decisions and code migration. They must remain intact so that:

1. We can always diff our new implementations against the originals
2. We can re-read reference code during later migration waves without worrying about drift
3. The reference projects can still be built and run independently for comparison testing

## Enforcement

- Code review: any PR touching `references/**` must be rejected
- CI: add a diff check that fails if `references/` is modified (future)

## How to Use References

- **Read** reference code to understand patterns and implementations
- **Copy** selected code into new modules under `grimoire-modules/` or `grimoire-applications/`
- **Adapt** copied code to fit the new layered architecture, Java 25, and Micronaut DI
- **Never** modify the originals

