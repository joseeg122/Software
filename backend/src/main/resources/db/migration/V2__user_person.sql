-- Usuario restringido a una sola persona: si person_id no es nulo, solo ve los datos de esa persona.
alter table users add column person_id bigint references persons (id);
