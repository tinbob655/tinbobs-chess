import React from 'react';


interface params {
    title: string,
    subtitle: string,
}

export default function PageHeader({title, subtitle}:params):React.ReactElement {

    const marginTopBottom:string = '30px';

    return (
        <React.Fragment>
            <h1 className="alignLeft noVerticalSpacing" style={{marginLeft: '10%', marginTop: marginTopBottom}}>
                {title}
            </h1>
            <p className="alignLeft noVerticalSpacing" style={{marginLeft: '12.5%', marginBottom: marginTopBottom}}>
                {subtitle}
            </p>
            <div className="dividerLine"></div>
        </React.Fragment>
    )
}