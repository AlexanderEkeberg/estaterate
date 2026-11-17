import {GraduationCap, Info} from "lucide-react";
import type {CategoryResult, SchoolData} from "../../types/report.js";
import {formatNumber} from "../../lib/format.js";
import {cx} from "../../lib/cx.js";
import {SectionPending, SectionUnavailable} from "./CategoryState.js";
import ui from "../../style/ui.module.css";
import styles from "./SchoolSection.module.css";

function formatDistance(km: number): string {
    return km < 1 ? `${Math.round(km * 1000)} m` : `${formatNumber(km, 1)} km`;
}

export function SchoolSection({
                                  result,
                              }: {
    result: CategoryResult<SchoolData>;
}) {
    const data = result.status === "AVAILABLE" ? result.data : undefined;

    return (
        <section
            aria-labelledby="school-title"
            className={cx(ui.card, styles.school)}
        >
            <div className={styles.sectionHead}>
                <div className={styles.sectionTitle}>
                    <span className={cx(ui.iconTile, ui.schoolsTile)}>
                        <GraduationCap size={22} aria-hidden/>
                    </span>
                    <div>
                        <h2 id="school-title" className={ui.h2}>
                            Schools
                        </h2>
                        <p className={styles.sectionSub}>
                            Distance to the nearest primary school
                        </p>
                    </div>
                </div>
                {data?.dataSource === "MOCK" && (
                    <span className={ui.sampleTag}>Sample data</span>
                )}
            </div>

            {data && <SchoolContent data={data}/>}

            {result.status === "UNAVAILABLE" && (
                <SectionUnavailable message="We couldn't load school data for this location right now."/>
            )}
            {result.status === "PENDING" && <SectionPending lines={2}/>}
        </section>
    );
}

function SchoolContent({data}: { data: SchoolData }) {
    const {nearestSchool, distanceKm, score} = data;

    return (
        <>
            {nearestSchool !== undefined && distanceKm !== undefined ? (
                <div className={styles.schoolCards}>
                    <div className={styles.schoolCard}>
                        <span className={styles.schoolCardLabel}>Nearest school</span>
                        <span className={styles.schoolCardValue}>{nearestSchool}</span>
                    </div>
                    <div className={styles.schoolCard}>
                        <span className={styles.schoolCardLabel}>Distance</span>
                        <span className={styles.schoolCardValue}>
              {formatDistance(distanceKm)}
            </span>
                    </div>
                    <div className={styles.schoolCard}>
                        <span className={styles.schoolCardLabel}>Score</span>
                        <span className={styles.schoolCardValue}>{score} / 100</span>
                    </div>
                </div>
            ) : (
                <p className={styles.sectionSub}>No school details available.</p>
            )}

            <div className={styles.sources}>
                <Info size={16} aria-hidden/>
                <span>Straight-line distance, Source: NSR (Utdanningsdirektoratet)</span>
            </div>
        </>
    );
}