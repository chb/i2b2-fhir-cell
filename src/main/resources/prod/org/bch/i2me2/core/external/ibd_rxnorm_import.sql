select au.tval_char as subject_id, ob.patient_num as patient_num, cd.concept_path as concept_path, ob.start_date as start_date, cd.name_char as name_char, ob.ENCOUNTER_NUM as encounter_num
from
  Observation_fact ob,
  concept_dimension cd,
  (select patient_num, TVAL_CHAR from observation_fact where concept_cd = 'IBDregv3|c93b1780-a7d2-11e2-a925-005056c00008|3590300055') au
where
  ob.CONCEPT_CD = cd.concept_cd and
  au.PATIENT_NUM = ob.PATIENT_NUM and
  cd.concept_cd in (
SELECT concept_cd
FROM concept_dimension where
  (name_char='Prior use' or name_char = 'Current use') and
  (concept_path like '%2691469841%' or
   concept_path like '%3101257421%' or
   concept_path like '%3712051085%' or
   concept_path like '%239830174%' or
   concept_path like '%2009516438%' or
   concept_path like '%3581222459%' or
   concept_path like '%3839019295%' or
   concept_path like '%3977947948%' or
   concept_path like '%2669683266%' or
   concept_path like '%442490628%' or
   concept_path like '%3271868671%' or
   concept_path like '%4233007851%' or
   concept_path like '%2000551791%' or
   concept_path like '%2696824670%' or
   concept_path like '%3267207501%' or
   concept_path like '%2952478642%' or
   concept_path like '%3861882691%' or
   concept_path like '%1534844979%' or
   concept_path like '%649339208%' or
   concept_path like '%2505365354%' or
   concept_path like '%1900935252%' or
   concept_path like '%4196626141%' or
   concept_path like '%4207939682%' or
   concept_path like '%3574751651%' or
   concept_path like '%2853493935%')
group by concept_cd)
group by au.tval_char, ob.patient_num, cd.concept_path, ob.start_date, cd.name_char, ob.ENCOUNTER_NUM
