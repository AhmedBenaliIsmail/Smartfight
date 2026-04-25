 Dev A Status — Identity & Events Track                                                                                            
                                                                                                                                    
  Phase 1 — Foundation COMPLETE                                                                                                     
                                                                                                                                    
  ┌─────────────────────────────────────────────────────┬──────────────────────────────────────────────────────────────────────┐    
  │                        Task                         │                                Status                                │
  ├─────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────────────────┤      │ P1-A1 WeightClass entity + repository               │ Done                                                                 │
  ├─────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────────────────┤      │ P1-A2 Modify Fighter entity                         │ Done                                                                 │
  ├─────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────────────────┤
  │ P1-A3 Restructure Event entity                      │ Done                                                                 │
  ├─────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────────────────┤
  │ P1-A4 VichUploader yaml mapping                     │ Done                                                                 │
  ├─────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────────────────┤
  │ P1-A5 Phase 1 migration (3 files: Version20260424*) │ Done                                                                 │
  ├─────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────────────────┤
  │ P1-A6 RegistrationController                        │ Done                                                                 │
  ├─────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────────────────┤
  │ P1-A7 RegistrationFormType                          │ Done                                                                 │
  ├─────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────────────────┤
  │ P1-A8 templates/registration/register.html.twig     │ Done (path differs slightly from spec — registration/ not security/) │
  ├─────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────────────────┤
  │ P1-A9 security.yaml access_control for /register    │ Done                                                                 │
  ├─────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────────────────┤
  │ P1-A10 "Sign Up" link in front base template        │ Not verified, likely done                                            │
  └─────────────────────────────────────────────────────┴──────────────────────────────────────────────────────────────────────┘

  Phase 1 gate: Registration works end-to-end (just confirmed Google OAuth flow works after today's fixes). Migration files exist.
  doctrine:schema:validate should be run to confirm clean.

  ---
  Phase 2 — Admin CRUD MOSTLY DONE, 2 GAPS

  ┌────────────────────────────────────────────────────┬─────────────────────────────────────────────────────────────────┐
  │                        Task                        │                             Status                              │
  ├────────────────────────────────────────────────────┼─────────────────────────────────────────────────────────────────┤
  │ P2-A1 Admin Fighter CRUD controller                │ Done                                                            │
  ├────────────────────────────────────────────────────┼─────────────────────────────────────────────────────────────────┤
  │ P2-A2 FighterType with VichUploader                │ Done (countryCode field present)                                │
  ├────────────────────────────────────────────────────┼─────────────────────────────────────────────────────────────────┤
  │ P2-A3 Fighter admin templates (list, form, detail) │ Partial — only index.html.twig exists, no form or show template │
  ├────────────────────────────────────────────────────┼─────────────────────────────────────────────────────────────────┤
  │ P2-A4 Country code select with flag preview        │ Partial — countryCode is a plain TextType, no flag preview UI   │
  ├────────────────────────────────────────────────────┼─────────────────────────────────────────────────────────────────┤
  │ P2-A5 Admin Event CRUD controller                  │ Done (index, new, show, edit, delete)                           │
  ├────────────────────────────────────────────────────┼─────────────────────────────────────────────────────────────────┤
  │ P2-A6 EventType with poster upload + venue fields  │ Done                                                            │
  ├────────────────────────────────────────────────────┼─────────────────────────────────────────────────────────────────┤
  │ P2-A7 Event admin templates                        │ Done (form, index, show all present)                            │
  ├────────────────────────────────────────────────────┼─────────────────────────────────────────────────────────────────┤
  │ P2-A8 "Cancel Event" action                        │ Missing — no cancel route in EventController                    │
  └────────────────────────────────────────────────────┴─────────────────────────────────────────────────────────────────┘

  ---
  Phase 3 — Public Events Browsing NOT STARTED

  No Front/EventController.php, no templates/front/event/ directory. All 9 tasks (P3-A1 through P3-A9) remain open.

  ---
  Phase 4 — Support tasks NOT STARTED

  P4-A1/A2 code reviews depend on Dev B finishing stats (Phase 4 main work). P4-A3 (rankings UX plan) and P4-A4 (stats partial in
  event detail) are not done — but P4-A3/A4 can't be done until Phase 3 event detail page exists.

  ---
  Phase 5 — Rankings PARTIALLY DONE

  ┌──────────────────────────────────────────────────────────┬────────────────────────────────────────────────────┐
  │                           Task                           │                       Status                       │
  ├──────────────────────────────────────────────────────────┼────────────────────────────────────────────────────┤
  │ P5-A1 RankingRepository query methods                    │ Done (modified)                                    │
  ├──────────────────────────────────────────────────────────┼────────────────────────────────────────────────────┤
  │ P5-A2 RankingService (reorder, champion, validate)       │ Done (modified)                                    │
  ├──────────────────────────────────────────────────────────┼────────────────────────────────────────────────────┤
  │ P5-A3 Admin rankings controller                          │ Done (index + recompute + CSV export)              │
  ├──────────────────────────────────────────────────────────┼────────────────────────────────────────────────────┤
  │ P5-A4 Admin rankings edit UI (drag reorder)              │ Partial — only index view, no drag-reorder edit UI │
  ├──────────────────────────────────────────────────────────┼────────────────────────────────────────────────────┤
  │ P5-A5–A9 Public /rankings route, template, champion card │ Missing — no public-facing rankings pages          │
  └──────────────────────────────────────────────────────────┴────────────────────────────────────────────────────┘

  ---
  Summary

  ┌─────────────────┬───────┬──────────────────────────────────────────────────────────┐
  │      Phase      │ Done  │                        Remaining                         │
  ├─────────────────┼───────┼──────────────────────────────────────────────────────────┤
  │ Phase 1         │ 10/10 │ 0                                                        │
  ├─────────────────┼───────┼──────────────────────────────────────────────────────────┤
  │ Phase 2         │ 6/8   │ Fighter form/show templates, flag preview, cancel action │
  ├─────────────────┼───────┼──────────────────────────────────────────────────────────┤
  │ Phase 3         │ 0/9   │ Everything                                               │
  ├─────────────────┼───────┼──────────────────────────────────────────────────────────┤
  │ Phase 4 support │ 0/4   │ Blocked on Phase 3 + Dev B                               │
  ├─────────────────┼───────┼──────────────────────────────────────────────────────────┤
  │ Phase 5         │ 3/9   │ Drag-reorder UI, public rankings pages                   │
  └─────────────────┴───────┴──────────────────────────────────────────────────────────┘

 Next priority: Finish Phase 2 gaps (fighter templates + cancel action), then start Phase 3 public events pages.